package dev.abdulrahman.excelsior.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.abdulrahman.excelsior.business.game.GameService;
import dev.abdulrahman.excelsior.presentation.exception.GameNotFoundException;
import dev.abdulrahman.excelsior.business.game.dto.GameRequestDto;
import dev.abdulrahman.excelsior.business.game.dto.GameResponseDto;
import dev.abdulrahman.excelsior.business.game.dto.GameTierPositionPatchDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import dev.abdulrahman.excelsior.presentation.exception.GameAlreadyExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import dev.abdulrahman.excelsior.business.game.dto.GameStatsDto;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import dev.abdulrahman.excelsior.presentation.config.WebConfig;
import dev.abdulrahman.excelsior.presentation.config.RateLimitInterceptor;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.abdulrahman.excelsior.business.game.GameTier;
import dev.abdulrahman.excelsior.business.game.GameStatus;

@WebMvcTest(GameController.class)
@Import({WebConfig.class, RateLimitInterceptor.class})
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private GameService gameService;

    @Test
    void addAllGames_ValidRequest_ReturnsCreated() throws Exception {
        GameRequestDto requestDto = new GameRequestDto(12345L, "Halo", 10.0, 15.0, LocalDate.of(2001, 11, 15),
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"));

        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(gameService, times(1)).addAllGames(anyList());
    }

    @Test
    void addGame_GamesAlreadyExists_ReturnsConflict() throws Exception {
        GameRequestDto requestDto = new GameRequestDto(12345L, "Halo", 10.0, 15.0, LocalDate.of(2001, 11, 15),
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"));

        doThrow(new GameAlreadyExistsException("Game with IGDB ID '12345' already exists.")).when(gameService).addAllGames(anyList());

        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Game Already Exists"))
                .andExpect(jsonPath("$.detail").value("Game with IGDB ID '12345' already exists."))
                .andExpect(jsonPath("$.status").value(409));

        verify(gameService, times(1)).addAllGames(anyList());
    }

    @Test
    void addAllGames_InvalidRequest_ReturnsBadRequest() throws Exception {
        GameRequestDto invalidDto = new GameRequestDto(12345L, "", 10.0, 15.0, LocalDate.of(2001, 11, 15),
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"));

        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(invalidDto))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Game name cannot be empty."));
        
        verify(gameService, never()).addAllGames(anyList());
    }

    @Test
    void getAllGames_ReturnsGameList() throws Exception {
        GameResponseDto responseDto = new GameResponseDto(1L, 123L, "Halo", 10.0, 15.0, LocalDate.of(2001, 11, 15), "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"), true, 0);
        Page<GameResponseDto> page = new PageImpl<>(List.of(responseDto));
        when(gameService.findGamesDynamically(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Halo"))
                .andExpect(jsonPath("$.content[0].length_normal").value(10.0));

        verify(gameService, times(1)).findGamesDynamically(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getStats_ReturnsGameStats() throws Exception {
        GameStatsDto stats = new GameStatsDto(
                50L,
                Map.of(GameStatus.COMPLETED, 30L, GameStatus.PLAYING, 20L),
                Map.of(GameTier.S, 10L)
        );
        when(gameService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/games/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total_games").value(50))
                .andExpect(jsonPath("$.games_by_status.COMPLETED").value(30))
                .andExpect(jsonPath("$.games_by_tier.S").value(10));

        verify(gameService, times(1)).getStats();
    }

    @Test
    void getGame_GameExists_ReturnsGame() throws Exception {
        GameResponseDto responseDto = new GameResponseDto(1L, 123L, "Halo", 10.0, 15.0, LocalDate.of(2001, 11, 15), "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"), true, 0);
        when(gameService.getGameById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/games/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Halo"));

        verify(gameService, times(1)).getGameById(1L);
    }

    @Test
    void getGame_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/games/{id}", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Game ID must be greater than 0."));
        
        verify(gameService, never()).getGameById(any());
    }

    @Test
    void getGame_GameDoesNotExist_ReturnsNotFound() throws Exception {
        when(gameService.getGameById(99L)).thenThrow(new GameNotFoundException("Could not find a game with ID '99'"));

        mockMvc.perform(get("/api/games/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Game Not Found"))
                .andExpect(jsonPath("$.detail").value("Could not find a game with ID '99'"))
                .andExpect(jsonPath("$.status").value(404));

        verify(gameService, times(1)).getGameById(99L);
    }

    @Test
    void updateGame_ValidRequest_ReturnsOk() throws Exception {
        GameRequestDto requestDto = new GameRequestDto(12345L, "Halo 2", 12.0, 15.0, LocalDate.of(2004, 11, 9),
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"));

        mockMvc.perform(put("/api/games/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(gameService, times(1)).updateGame(eq(1L), any(GameRequestDto.class));
    }

    @Test
    void updateGame_GameDoesNotExist_ReturnsNotFound() throws Exception {
        GameRequestDto requestDto = new GameRequestDto(12345L, "Halo 2", 12.0, 15.0, LocalDate.of(2004, 11, 9),
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"));
        
        doThrow(new GameNotFoundException("Could not find a game with ID '99'")).when(gameService).updateGame(eq(99L), any(GameRequestDto.class));

        mockMvc.perform(put("/api/games/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Game Not Found"))
                .andExpect(jsonPath("$.detail").value("Could not find a game with ID '99'"))
                .andExpect(jsonPath("$.status").value(404));

        verify(gameService, times(1)).updateGame(eq(99L), any(GameRequestDto.class));
    }

    @Test
    void deleteGame_GameExists_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/games/{id}", 1L))
                .andExpect(status().isOk());

        verify(gameService, times(1)).deleteGame(1L);
    }

    @Test
    void deleteGame_GameDoesNotExist_ReturnsNotFound() throws Exception {
        doThrow(new GameNotFoundException("Could not find a game with ID '99'")).when(gameService).deleteGame(99L);

        mockMvc.perform(delete("/api/games/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Game Not Found"))
                .andExpect(jsonPath("$.detail").value("Could not find a game with ID '99'"))
                .andExpect(jsonPath("$.status").value(404));

        verify(gameService, times(1)).deleteGame(99L);
    }

    @Test
    void searchGame_ValidRequest_ReturnsGameResponseDto() throws Exception {
        GameResponseDto responseDto = new GameResponseDto(1L, 123L, "God of War", 10.0, 15.0, LocalDate.of(2005, 3, 22), "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Santa Monica"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"), true, 0);
        when(gameService.searchGame("God of War", null, null)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/games/search")
                .param("query", "God of War")
                .with(request -> { request.setRemoteAddr("1.1.1.1"); return request; })
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("God of War"));

        verify(gameService, times(1)).searchGame("God of War", null, null);
    }

    @Test
    void searchGame_RateLimitExceeded_ReturnsTooManyRequests() throws Exception {
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(get("/api/games/search")
                    .param("query", "God of War")
                    .with(request -> { request.setRemoteAddr("2.2.2.2"); return request; })
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/games/search")
                .param("query", "God of War")
                .with(request -> { request.setRemoteAddr("2.2.2.2"); return request; })
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.title").value("Too Many Requests"))
                .andExpect(jsonPath("$.detail").value("You are searching too fast. Please wait a few seconds before trying again."));
        
        verify(gameService, times(20)).searchGame("God of War", null, null);
    }

    @Test
    void patchGame_ValidRequest_ReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/games/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tier\": \"S\"}"))
                .andExpect(status().isOk());

        verify(gameService, times(1)).updateTier(1L, GameTier.S);
    }
    @Test
    void patchTierPositions_ValidRequest_ReturnsOk() throws Exception {
        GameTierPositionPatchDto dto1 = new GameTierPositionPatchDto(1L, GameTier.S, 1);
        GameTierPositionPatchDto dto2 = new GameTierPositionPatchDto(2L, GameTier.A, 0);
        List<GameTierPositionPatchDto> dtos = List.of(dto1, dto2);

        mockMvc.perform(patch("/api/games/tier-positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtos)))
                .andExpect(status().isOk());

        verify(gameService, times(1)).updateTierPositionForAll(dtos);
    }
}
