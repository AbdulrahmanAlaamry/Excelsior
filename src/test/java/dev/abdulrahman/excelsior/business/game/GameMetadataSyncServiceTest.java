package dev.abdulrahman.excelsior.business.game;

import dev.abdulrahman.excelsior.business.game.dto.GameSyncDto;
import dev.abdulrahman.excelsior.business.user.User;
import dev.abdulrahman.excelsior.integration.ClientService;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import dev.abdulrahman.excelsior.presentation.exception.TwitchAuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionOperations;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameMetadataSyncServiceTest {

    @Mock
    private ClientService client;

    @Mock
    private GameRepository repository;

    private GameMetadataSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new GameMetadataSyncService(client, repository, TransactionOperations.withoutTransaction());
    }

    @Test
    void syncMetadata_UpdatesMetadataAcrossUsersWhilePreservingUserFields() {
        User user1 = new User("alice", "pass1");
        User user2 = new User("bob", "pass2");

        Game aliceGame = new Game(555L, "Phantom Blade Zero", 0.0, 0.0, LocalDate.of(2026, 9, 9),
                "old_cover", null, null, "old synopsis", "old desc", "UE5",
                List.of("Action"), List.of("S-Game"), GameTier.S, GameStatus.PLAYING, List.of("old_ss"), List.of("PS5"), user1);
        aliceGame.setTierPosition(3);
        aliceGame.setMetadataUpdatedAt(null);

        Game bobGame = new Game(555L, "Phantom Blade Zero", 0.0, 0.0, LocalDate.of(2026, 9, 9),
                "old_cover", null, null, "old synopsis", "old desc", "UE5",
                List.of("Action"), List.of("S-Game"), GameTier.B, GameStatus.BACKLOG, List.of("old_ss"), List.of("PS5"), user2);
        bobGame.setTierPosition(7);

        GameSyncDto freshDto = new GameSyncDto(
                "Phantom Blade Zero", 22.5, 40.0, LocalDate.of(2026, 9, 9),
                "new_cover", 89.4, 420, "new synopsis", "new desc", "Unreal Engine 5",
                List.of("Action RPG"), List.of("S-GAME"), List.of("new_ss"), List.of("PS5", "PC")
        );

        when(client.fetchGame(555L)).thenReturn(freshDto);
        when(repository.findAllByIgdbId(555L)).thenReturn(List.of(aliceGame, bobGame));

        syncService.syncMetadata(555L);

        assertEquals(89.4, aliceGame.getRating());
        assertEquals(420, aliceGame.getRatingCount());
        assertEquals(22.5, aliceGame.getLengthNormal());
        assertNotNull(aliceGame.getMetadataUpdatedAt());
        assertEquals(GameTier.S, aliceGame.getTierValue());
        assertEquals(GameStatus.PLAYING, aliceGame.getStatus());
        assertEquals(3, aliceGame.getTierPosition());
        assertEquals(user1, aliceGame.getUser());

        assertEquals(89.4, bobGame.getRating());
        assertEquals(GameTier.B, bobGame.getTierValue());
        assertEquals(GameStatus.BACKLOG, bobGame.getStatus());
        assertEquals(7, bobGame.getTierPosition());
        assertEquals(user2, bobGame.getUser());

        verify(repository, times(1)).saveAll(List.of(aliceGame, bobGame));
    }

    @Test
    void syncMetadata_UpstreamFailure_LogsAndSwallowsException() {
        when(client.fetchGame(999L)).thenThrow(new TwitchAuthenticationException("IGDB down"));

        assertDoesNotThrow(() -> syncService.syncMetadata(999L));
        verify(repository, never()).findAllByIgdbId(any());
        verify(repository, never()).saveAll(any());
    }

    @Test
    void syncMetadata_RapidRepeatedCalls_DeduplicatesWithinCooldown() {
        GameSyncDto freshDto = new GameSyncDto(
                "Game", 10.0, 15.0, LocalDate.of(2025, 1, 1),
                "cover", 80.0, 100, "syn", "desc", "UE5",
                List.of("RPG"), List.of("Studio"), List.of("ss"), List.of("PC")
        );
        when(client.fetchGame(777L)).thenReturn(freshDto);
        when(repository.findAllByIgdbId(777L)).thenReturn(List.of());

        syncService.syncMetadata(777L);
        syncService.syncMetadata(777L);

        verify(client, times(1)).fetchGame(777L);
    }

    @Test
    void syncMetadata_ConcurrentCallsForSameIgdbId_OnlyFetchesOnce() throws InterruptedException {
        CountDownLatch fetchStarted = new CountDownLatch(1);
        CountDownLatch allowFinish = new CountDownLatch(1);

        GameSyncDto freshDto = new GameSyncDto(
                "Game", 10.0, 15.0, LocalDate.of(2025, 1, 1),
                "cover", 80.0, 100, "syn", "desc", "UE5",
                List.of("RPG"), List.of("Studio"), List.of("ss"), List.of("PC")
        );

        when(client.fetchGame(888L)).thenAnswer(invocation -> {
            fetchStarted.countDown();
            assertTrue(allowFinish.await(5, TimeUnit.SECONDS));
            return freshDto;
        });
        when(repository.findAllByIgdbId(888L)).thenReturn(List.of());

        Thread first = new Thread(() -> syncService.syncMetadata(888L));
        first.start();
        assertTrue(fetchStarted.await(5, TimeUnit.SECONDS));

        // Second call arrives while first is still in flight
        syncService.syncMetadata(888L);

        allowFinish.countDown();
        first.join(5000);

        verify(client, times(1)).fetchGame(888L);
    }
}
