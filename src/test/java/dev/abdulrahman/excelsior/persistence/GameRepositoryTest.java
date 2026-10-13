package dev.abdulrahman.excelsior.persistence;

import dev.abdulrahman.excelsior.business.game.Game;
import dev.abdulrahman.excelsior.business.game.GameStatus;
import dev.abdulrahman.excelsior.business.game.GameTier;
import dev.abdulrahman.excelsior.business.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;
    private Game chronoTrigger;
    private Game superMario;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User("tester", "password"));
        otherUser = userRepository.save(new User("otherTester", "password"));

        chronoTrigger = new Game(
                123L, "Chrono Trigger", 25.0, 40.0,
                LocalDate.of(1995, 3, 11), "coverUrl1", 95.0, 500,
                "A time-travel adventure", "Full description", "Proprietary",
                List.of("JRPG", "Adventure"), List.of("Square"),
                GameTier.S, GameStatus.COMPLETED,
                List.of("screen1"), List.of("SNES"), testUser
        );
        gameRepository.save(chronoTrigger);

        superMario = new Game(
                456L, "Super Mario World", 10.0, 15.0,
                LocalDate.of(1990, 11, 21), "coverUrl2", 90.0, 800,
                "Platforming fun", "Mario description", "Nintendo Engine",
                List.of("Platformer"), List.of("Nintendo"),
                GameTier.A, GameStatus.PLAYING,
                List.of("screen2"), List.of("SNES", "GBA"), testUser
        );
        gameRepository.save(superMario);

        Game otherGame = new Game(
                789L, "Doom", 8.0, 12.0,
                LocalDate.of(1993, 12, 10), "coverUrl3", 85.0, 1200,
                "FPS classic", "Doom desc", "Id Tech",
                List.of("Shooter"), List.of("id Software"),
                GameTier.B, GameStatus.BACKLOG,
                List.of("screen3"), List.of("PC"), otherUser
        );
        gameRepository.save(otherGame);
    }

    @Test
    void findDynamically_AllNullParameters_ReturnsOnlyCurrentUserGames() {
        Page<Game> page = gameRepository.findDynamically(
                testUser, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                PageRequest.of(0, 10)
        );
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void findDynamically_FilterByNameContains_CaseInsensitive() {
        Page<Game> page = gameRepository.findDynamically(
                testUser, null, null, "chrono", null,
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                PageRequest.of(0, 10)
        );
        assertEquals(1, page.getTotalElements());
        assertEquals("Chrono Trigger", page.getContent().get(0).getName());
    }

    @Test
    void findDynamically_FilterByRatingRange() {
        Page<Game> page = gameRepository.findDynamically(
                testUser, 92.0, 99.0, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                PageRequest.of(0, 10)
        );
        assertEquals(1, page.getTotalElements());
        assertEquals("Chrono Trigger", page.getContent().get(0).getName());
    }

    @Test
    void findDynamically_FilterByGenre() {
        Page<Game> page = gameRepository.findDynamically(
                testUser, null, null, null, "Platformer",
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                PageRequest.of(0, 10)
        );
        assertEquals(1, page.getTotalElements());
        assertEquals("Super Mario World", page.getContent().get(0).getName());
    }

    @Test
    void findDynamically_FilterByPlatform() {
        Page<Game> page = gameRepository.findDynamically(
                testUser, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, "GBA",
                PageRequest.of(0, 10)
        );
        assertEquals(1, page.getTotalElements());
        assertEquals("Super Mario World", page.getContent().get(0).getName());
    }

    @Test
    void findDynamically_FilterByLength() {
        Page<Game> page = gameRepository.findDynamically(
                testUser, null, null, null, null,
                20.0, 30.0, 35.0, 50.0,
                null, null, null, null,
                null, null, null,
                PageRequest.of(0, 10)
        );
        assertEquals(1, page.getTotalElements());
        assertEquals("Chrono Trigger", page.getContent().get(0).getName());
    }

    @Test
    void findDynamically_FilterByDateAndYear() {
        Page<Game> page = gameRepository.findDynamically(
                testUser, null, null, null, null,
                null, null, null, null,
                LocalDate.of(1990, 1, 1), LocalDate.of(1991, 1, 1),
                1990, 1990,
                null, null, null,
                PageRequest.of(0, 10)
        );
        assertEquals(1, page.getTotalElements());
        assertEquals("Super Mario World", page.getContent().get(0).getName());
    }

    @Test
    void findDynamically_FilterByTierAndStatus() {
        Page<Game> page = gameRepository.findDynamically(
                testUser, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                GameTier.S, GameStatus.COMPLETED, null,
                PageRequest.of(0, 10)
        );
        assertEquals(1, page.getTotalElements());
        assertEquals("Chrono Trigger", page.getContent().get(0).getName());
    }

    @Test
    void findDynamically_SortingByRatingDescending() {
        Page<Game> page = gameRepository.findDynamically(
                testUser, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "rating"))
        );
        assertEquals(2, page.getTotalElements());
        assertEquals("Chrono Trigger", page.getContent().get(0).getName());
        assertEquals("Super Mario World", page.getContent().get(1).getName());
    }
}
