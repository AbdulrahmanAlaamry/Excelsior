package dev.abdulrahman.excelsior.business;

import dev.abdulrahman.excelsior.business.game.*;
import dev.abdulrahman.excelsior.business.game.dto.GameRequestDto;
import dev.abdulrahman.excelsior.business.game.dto.GameResponseDto;
import dev.abdulrahman.excelsior.business.game.dto.GameStatsDto;
import dev.abdulrahman.excelsior.business.game.dto.GameTierPositionPatchDto;
import dev.abdulrahman.excelsior.presentation.exception.GameNotFoundException;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import dev.abdulrahman.excelsior.business.user.User;
import dev.abdulrahman.excelsior.persistence.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DtoMapper mapper;

    @InjectMocks
    private GameService gameService;

    @Captor
    private ArgumentCaptor<Game> gameCaptor;

    private Game game;
    private GameRequestDto requestDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "pass");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getName()).thenReturn("testuser");
        lenient().when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        game = new Game(123L, "Super Mario", 15.0, 20.0, LocalDate.of(1985, 9, 13),
                "cover_url", 95.0, 1000, "synopsis", "desc", "engine", List.of("Platformer"), List.of("Nintendo"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"), testUser);
        game.setId(1L);

        requestDto = new GameRequestDto(123L, "Super Mario", 15.0, 20.0, LocalDate.of(1985, 9, 13),
                "cover_url", 95.0, 1000, "synopsis", "desc", "engine", List.of("Platformer"), List.of("Nintendo"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"));
    }

    @Test
    void addGame_SavesGame() {
        when(mapper.toGameEntity(requestDto, testUser)).thenReturn(game);
        
        gameService.addGame(requestDto);

        verify(gameRepository, times(1)).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();
        assertEquals("Super Mario", savedGame.getName());
        assertEquals(15.0, savedGame.getLengthNormal());
        assertEquals(LocalDate.of(1985, 9, 13), savedGame.getReleaseDate());
    }

    @Test
    void findGamesDynamically_ReturnsListOfGames() {
        Page<Game> page = new PageImpl<>(List.of(game));
        when(gameRepository.findDynamically(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);
        GameResponseDto responseDto = new GameResponseDto(1L, 123L, "Super Mario", 15.0, 20.0, LocalDate.of(1985, 9, 13), "cover", 95.0, 1000, "synopsis", "desc", "engine", List.of("Platformer"), List.of("Nintendo"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"), true, 0);
        when(mapper.toGameResponseDto(game)).thenReturn(responseDto);

        Page<GameResponseDto> results = gameService.findGamesDynamically(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertEquals(1, results.getContent().size());
        assertEquals("Super Mario", results.getContent().get(0).name());
        assertEquals(15.0, results.getContent().get(0).lengthNormal());
        verify(gameRepository, times(1)).findDynamically(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getGameById_GameExists_ReturnsGame() {
        when(gameRepository.findByUserAndId(testUser, 1L)).thenReturn(Optional.of(game));
        GameResponseDto responseDto = new GameResponseDto(1L, 123L, "Super Mario", 15.0, 20.0, LocalDate.of(1985, 9, 13), "cover", 95.0, 1000, "synopsis", "desc", "engine", List.of("Platformer"), List.of("Nintendo"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"), true, 0);
        when(mapper.toGameResponseDto(game)).thenReturn(responseDto);

        GameResponseDto result = gameService.getGameById(1L);

        assertNotNull(result);
        assertEquals("Super Mario", result.name());
        verify(gameRepository, times(1)).findByUserAndId(testUser, 1L);
    }

    @Test
    void getGameById_GameDoesNotExist_ThrowsException() {
        when(gameRepository.findByUserAndId(testUser, 99L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.getGameById(99L));
        verify(gameRepository, times(1)).findByUserAndId(testUser, 99L);
    }

    @Test
    void updateGame_GameExists_UpdatesAndSavesGame() {
        when(gameRepository.findByUserAndId(testUser, 1L)).thenReturn(Optional.of(game));
        GameRequestDto updateDto = new GameRequestDto(123L, "Super Mario Bros.", 20.0, 25.0, LocalDate.of(1985, 9, 13),
                "new_cover", 96.0, 1500, "new_synopsis", "new_desc", "engine", List.of("Platformer"), List.of("Nintendo"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"));

        gameService.updateGame(1L, updateDto);

        verify(gameRepository, times(1)).save(game);
        assertEquals("Super Mario Bros.", game.getName());
        assertEquals(20.0, game.getLengthNormal());
    }

    @Test
    void updateGame_GameDoesNotExist_ThrowsException() {
        when(gameRepository.findByUserAndId(testUser, 2L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.updateGame(2L, requestDto));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void deleteGame_GameExists_DeletesGame() {
        when(gameRepository.findByUserAndId(testUser, 1L)).thenReturn(Optional.of(game));

        gameService.deleteGame(1L);

        verify(gameRepository, times(1)).delete(game);
    }

    @Test
    void deleteGame_GameDoesNotExist_ThrowsException() {
        when(gameRepository.findByUserAndId(testUser, 2L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.deleteGame(2L));
        verify(gameRepository, never()).delete(any());
    }

    @Test
    void getStats_ReturnsGameStats() {
        Game game2 = new Game(124L, "Zelda", 20.0, 30.0, LocalDate.of(1986, 2, 21),
                "cover2", 98.0, 2000, "synopsis", "desc", "engine", List.of("Adventure"), List.of("Nintendo"), GameTier.A, GameStatus.PLAYING, List.of("url2"), List.of("Switch"), testUser);
        when(gameRepository.findAllByUser(testUser)).thenReturn(List.of(game, game2));

        GameStatsDto stats = gameService.getStats();

        assertNotNull(stats);
        assertEquals(2L, stats.totalGames());
        assertEquals(1L, stats.gamesByStatus().get(GameStatus.COMPLETED));
        assertEquals(1L, stats.gamesByStatus().get(GameStatus.PLAYING));
        assertEquals(1L, stats.gamesByTier().get(GameTier.S));
        assertEquals(1L, stats.gamesByTier().get(GameTier.A));
    }
    @Test
    void updateTierPositionForAll_UpdatesPositions() {
        GameTierPositionPatchDto dto1 = new GameTierPositionPatchDto(1L, GameTier.S, 1);
        GameTierPositionPatchDto dto2 = new GameTierPositionPatchDto(124L, GameTier.A, 0);
        List<GameTierPositionPatchDto> dtos = List.of(dto1, dto2);

        Game game2 = new Game(124L, "Zelda", 20.0, 30.0, LocalDate.of(1986, 2, 21),
                "cover2", 98.0, 2000, "synopsis", "desc", "engine", List.of("Adventure"), List.of("Nintendo"), GameTier.A, GameStatus.PLAYING, List.of("url2"), List.of("Switch"), testUser);
        when(gameRepository.findByUserAndId(testUser, 1L)).thenReturn(Optional.of(game));
        when(gameRepository.findByUserAndId(testUser, 124L)).thenReturn(Optional.of(game2));

        gameService.updateTierPositionForAll(dtos);

        verify(gameRepository, times(1)).save(game);
        verify(gameRepository, times(1)).save(game2);
        assertEquals(GameTier.S, game.getTierValue());
        assertEquals(1, game.getTierPosition());
        assertEquals(GameTier.A, game2.getTierValue());
        assertEquals(0, game2.getTierPosition());
    }
}
