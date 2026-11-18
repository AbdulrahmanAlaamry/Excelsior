package dev.abdulrahman.excelsior.business.game;

import dev.abdulrahman.excelsior.business.game.dto.GameSyncDto;
import dev.abdulrahman.excelsior.integration.ClientService;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameMetadataSyncService {

    private static final Logger log = LoggerFactory.getLogger(GameMetadataSyncService.class);
    private static final Duration SYNC_COOLDOWN = Duration.ofMinutes(1);
    private static final int MAX_RECENT_ATTEMPTS_CACHE_SIZE = 1000;

    private final ClientService client;
    private final GameRepository repository;
    private final TransactionOperations transactionOperations;
    private final Set<Long> inFlightIgdbIds = ConcurrentHashMap.newKeySet();
    private final Map<Long, Instant> recentAttempts = new ConcurrentHashMap<>();

    public GameMetadataSyncService(
            ClientService client,
            GameRepository repository,
            TransactionOperations transactionOperations
    ) {
        this.client = client;
        this.repository = repository;
        this.transactionOperations = transactionOperations;
    }

    @Async("metadataSyncExecutor")
    public void syncMetadata(Long igdbId) {
        if (igdbId == null || !inFlightIgdbIds.add(igdbId)) {
            return;
        }

        try {
            Instant now = Instant.now();
            Instant lastAttempt = recentAttempts.get(igdbId);
            if (lastAttempt != null && lastAttempt.isAfter(now.minus(SYNC_COOLDOWN))) {
                return;
            }

            pruneRecentAttemptsIfNeeded(now);
            recentAttempts.put(igdbId, now);

            GameSyncDto dto = client.fetchGame(igdbId);

            transactionOperations.executeWithoutResult(status -> {
                List<Game> staleGames = repository.findAllByIgdbId(igdbId);
                if (staleGames.isEmpty()) {
                    return;
                }
                staleGames.forEach(game -> game.sync(dto));
                repository.saveAll(staleGames);
                log.info("Synced IGDB metadata for {} library record(s) with IGDB ID {}", staleGames.size(), igdbId);
            });
        } catch (Exception ex) {
            log.warn("Background metadata sync failed for IGDB ID {}", igdbId, ex);
        } finally {
            inFlightIgdbIds.remove(igdbId);
        }
    }

    private void pruneRecentAttemptsIfNeeded(Instant now) {
        if (recentAttempts.size() > MAX_RECENT_ATTEMPTS_CACHE_SIZE) {
            Instant cutoff = now.minus(SYNC_COOLDOWN);
            recentAttempts.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        }
    }
}
