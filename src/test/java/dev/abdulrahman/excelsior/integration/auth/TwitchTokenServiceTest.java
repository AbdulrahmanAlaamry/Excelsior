package dev.abdulrahman.excelsior.integration.auth;

import dev.abdulrahman.excelsior.persistence.OAuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwitchTokenServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private OAuthTokenRepository tokenRepository;

    private TwitchTokenService tokenService;

    @BeforeEach
    void setUp() {
        TransactionOperations noTx = TransactionOperations.withoutTransaction();
        tokenService = new TwitchTokenService(authService, tokenRepository, noTx, noTx);
    }

    @Test
    void getAccessToken_CachesInMemoryAcrossSubsequentCalls() {
        when(tokenRepository.findById("twitch")).thenReturn(Optional.empty());
        when(authService.fetchTokenDetails()).thenReturn(new AuthResponse("tok-1", 3600, "bearer"));

        String first = tokenService.getAccessToken();
        String second = tokenService.getAccessToken();
        String third = tokenService.getAccessToken();

        assertEquals("tok-1", first);
        assertEquals("tok-1", second);
        assertEquals("tok-1", third);
        verify(authService, times(1)).fetchTokenDetails();
        verify(tokenRepository, times(2)).findById("twitch"); // once on load, once on upsert
    }

    @Test
    void getAccessToken_ValidPersistedTokenInDb_ReusesWithoutCallingTwitch() {
        OAuthToken persisted = new OAuthToken("twitch", "persisted-tok", Instant.now().plusSeconds(86400));
        when(tokenRepository.findById("twitch")).thenReturn(Optional.of(persisted));

        String token = tokenService.getAccessToken();

        assertEquals("persisted-tok", token);
        verifyNoInteractions(authService);
    }

    @Test
    void getAccessToken_PersistedTokenExpiringWithinBuffer_FetchesFreshTokenAndUpdatesDb() {
        OAuthToken expiring = new OAuthToken("twitch", "old-tok", Instant.now().plusSeconds(120));
        when(tokenRepository.findById("twitch")).thenReturn(Optional.of(expiring));
        when(authService.fetchTokenDetails()).thenReturn(new AuthResponse("new-tok", 5000000, "bearer"));

        String token = tokenService.getAccessToken();

        assertEquals("new-tok", token);
        verify(authService, times(1)).fetchTokenDetails();

        ArgumentCaptor<OAuthToken> savedCaptor = ArgumentCaptor.forClass(OAuthToken.class);
        verify(tokenRepository).save(savedCaptor.capture());
        assertEquals("new-tok", savedCaptor.getValue().getAccessToken());
        assertTrue(savedCaptor.getValue().getExpiresAt().isAfter(Instant.now().plusSeconds(4999000)));
    }

    @Test
    void invalidateToken_StaleTokenDoesNotEvictNewerToken() {
        OAuthToken persisted = new OAuthToken("twitch", "fresh-tok", Instant.now().plusSeconds(86400));
        when(tokenRepository.findById("twitch")).thenReturn(Optional.of(persisted));

        assertEquals("fresh-tok", tokenService.getAccessToken());

        // Thread B reporting an older failed token must not clear the fresh token
        tokenService.invalidateToken("old-tok");
        assertEquals("fresh-tok", tokenService.getAccessToken());
        verify(tokenRepository, never()).delete(any());

        // Reporting the actual current token clears both L1 cache and DB row
        tokenService.invalidateToken("fresh-tok");
        verify(tokenRepository).delete(persisted);
    }
}
