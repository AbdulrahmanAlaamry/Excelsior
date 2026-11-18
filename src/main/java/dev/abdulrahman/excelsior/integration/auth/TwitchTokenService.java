package dev.abdulrahman.excelsior.integration.auth;

import dev.abdulrahman.excelsior.persistence.OAuthTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;

/**
 * Provides thread-safe, two-tier (in-memory + PostgreSQL) caching for Twitch OAuth2 access tokens
 * with proactive expiration checks so tokens are reused across restarts until they expire.
 */
@Service
public class TwitchTokenService {

    private static final Logger log = LoggerFactory.getLogger(TwitchTokenService.class);
    private static final String TWITCH_PROVIDER = "twitch";
    private static final Duration EXPIRY_BUFFER = Duration.ofMinutes(5);
    private static final long DEFAULT_EXPIRES_IN_SECONDS = 3600L;

    private final AuthService authService;
    private final OAuthTokenRepository tokenRepository;
    private final TransactionOperations readTx;
    private final TransactionOperations writeTx;
    private final Object lock = new Object();
    private volatile CachedToken cachedToken;

    @Autowired
    public TwitchTokenService(
            AuthService authService,
            OAuthTokenRepository tokenRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.authService = authService;
        this.tokenRepository = tokenRepository;

        TransactionTemplate readTemplate = new TransactionTemplate(transactionManager);
        readTemplate.setReadOnly(true);
        this.readTx = readTemplate;

        // REQUIRES_NEW prevents joining an outer readOnly=true transaction (e.g. GameService.searchGame),
        // which would otherwise stop Hibernate from flushing the token insert to PostgreSQL.
        TransactionTemplate writeTemplate = new TransactionTemplate(transactionManager);
        writeTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        writeTemplate.setReadOnly(false);
        this.writeTx = writeTemplate;
    }

    TwitchTokenService(
            AuthService authService,
            OAuthTokenRepository tokenRepository,
            TransactionOperations readTx,
            TransactionOperations writeTx
    ) {
        this.authService = authService;
        this.tokenRepository = tokenRepository;
        this.readTx = readTx;
        this.writeTx = writeTx;
    }

    public String getAccessToken() {
        CachedToken current = cachedToken;
        if (current != null && !current.isExpiringSoon()) {
            return current.accessToken();
        }

        synchronized (lock) {
            current = cachedToken;
            if (current != null && !current.isExpiringSoon()) {
                return current.accessToken();
            }

            CachedToken persisted = loadValidPersistedToken();
            if (persisted != null) {
                log.info("Loaded valid Twitch OAuth2 token from database (expires at {})", persisted.expiresAt());
                cachedToken = persisted;
                return persisted.accessToken();
            }

            AuthResponse response = authService.fetchTokenDetails();
            long expiresIn = response.expiresIn() != null && response.expiresIn() > 0
                    ? response.expiresIn()
                    : DEFAULT_EXPIRES_IN_SECONDS;
            Instant expiresAt = Instant.now().plusSeconds(expiresIn);

            CachedToken refreshed = new CachedToken(response.accessToken(), expiresAt);
            persistToken(refreshed);
            cachedToken = refreshed;
            return refreshed.accessToken();
        }
    }

    /**
     * Invalidates the cached and persisted token only if it matches the token that failed with HTTP 401,
     * preventing concurrent threads from wiping out an already-refreshed token.
     */
    public void invalidateToken(String failedToken) {
        synchronized (lock) {
            if (cachedToken != null && !cachedToken.accessToken().equals(failedToken)) {
                return;
            }
            cachedToken = null;
            writeTx.executeWithoutResult(status ->
                    tokenRepository.findById(TWITCH_PROVIDER).ifPresent(entity -> {
                        if (entity.getAccessToken().equals(failedToken)) {
                            tokenRepository.delete(entity);
                        }
                    })
            );
        }
    }

    private CachedToken loadValidPersistedToken() {
        return readTx.execute(status ->
                tokenRepository.findById(TWITCH_PROVIDER)
                        .map(entity -> new CachedToken(entity.getAccessToken(), entity.getExpiresAt()))
                        .filter(token -> !token.isExpiringSoon())
                        .orElse(null)
        );
    }

    private void persistToken(CachedToken token) {
        writeTx.executeWithoutResult(status -> {
            OAuthToken entity = tokenRepository.findById(TWITCH_PROVIDER)
                    .orElseGet(() -> new OAuthToken(TWITCH_PROVIDER, token.accessToken(), token.expiresAt()));
            entity.setAccessToken(token.accessToken());
            entity.setExpiresAt(token.expiresAt());
            tokenRepository.save(entity);
        });
    }

    private record CachedToken(String accessToken, Instant expiresAt) {
        boolean isExpiringSoon() {
            return expiresAt == null || Instant.now().isAfter(expiresAt.minus(EXPIRY_BUFFER));
        }
    }
}
