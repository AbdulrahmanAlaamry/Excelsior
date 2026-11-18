package dev.abdulrahman.excelsior.business.game;

import dev.abdulrahman.excelsior.business.game.dto.GameSyncDto;
import dev.abdulrahman.excelsior.business.user.User;
import dev.abdulrahman.excelsior.integration.ClientService;
import dev.abdulrahman.excelsior.integration.dto.*;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import dev.abdulrahman.excelsior.persistence.UserRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class GameMetadataSyncIntegrationTest {

    @Autowired
    private GameMetadataSyncService syncService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private TransactionOperations transactionOperations;

    @MockitoBean
    private ClientService clientService;

    @Test
    void syncMetadata_WithRealJpaAndAsyncExecutor_UpdatesElementCollectionsAndScalarFields() {
        User user = userRepository.save(new User("syncUser_" + System.currentTimeMillis(), "Password123"));
        Game saved = gameRepository.save(new Game(
                250618L, "Phantom Blade Zero", 0.0, 0.0, LocalDate.of(2026, 9, 9),
                "old_cover", 0.0, 0, "old synopsis", "old desc", "UE5",
                List.of("Action"), List.of("S-Game"), GameTier.S, GameStatus.PLAYING,
                List.of("old_ss"), List.of("PS5"), user
        ));
        saved.setTierPosition(4);
        saved.setMetadataUpdatedAt(null);
        gameRepository.save(saved);

        GameDto apiGame = new GameDto(
                250618,
                new ImageDto("//images.igdb.com/igdb/image/upload/t_thumb/co1234.jpg", 600, 800),
                1788912000L,
                "Phantom Blade Zero",
                "phantom-blade-zero",
                "Storyline",
                "Summary",
                88.5,
                150,
                new EngineDto[]{new EngineDto("Unreal Engine 5")},
                new GenreDto[]{new GenreDto("Role-playing (RPG)"), new GenreDto("Hack and slash")},
                new InvolvedCompanyDto[]{new InvolvedCompanyDto(new CompanyDto("S-GAME"))},
                new ImageDto[]{new ImageDto("//images.igdb.com/igdb/image/upload/t_thumb/sc1234.jpg", 1920, 1080)},
                new PlatformDto[]{new PlatformDto("PlayStation 5"), new PlatformDto("PC (Microsoft Windows)")}
        );
        GameSyncDto syncDto = mapper.toGameSyncDto(apiGame, true, new TimeToBeatDto(250618, 108000, 72000));

        when(clientService.fetchGame(250618L)).thenReturn(syncDto);

        syncService.syncMetadata(250618L);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> transactionOperations.executeWithoutResult(status -> {
                    Game updated = gameRepository.findById(saved.getId()).orElseThrow();
                    assertEquals(88.5, updated.getRating());
                    assertEquals(150, updated.getRatingCount());
                    assertEquals(20.0, updated.getLengthNormal());
                    assertEquals(30.0, updated.getLengthComplete());
                    assertNotNull(updated.getMetadataUpdatedAt());
                    assertEquals(List.of("Role-playing (RPG)", "Hack and slash"), updated.getGenres());
                    assertEquals(List.of("PlayStation 5", "PC (Microsoft Windows)"), updated.getPlatforms());
                    assertEquals(GameTier.S, updated.getTierValue());
                    assertEquals(GameStatus.PLAYING, updated.getStatus());
                    assertEquals(4, updated.getTierPosition());
                }));
    }
}
