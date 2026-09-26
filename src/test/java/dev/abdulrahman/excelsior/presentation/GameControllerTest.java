package dev.abdulrahman.excelsior.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.abdulrahman.excelsior.business.GameService;
import dev.abdulrahman.excelsior.presentation.exception.GameNotFoundException;
import dev.abdulrahman.excelsior.business.game.GameRequestDto;
import dev.abdulrahman.excelsior.business.game.GameResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

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
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url1"), List.of("url2"), List.of("PC"));

        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(gameService, times(1)).addAllGames(anyList());
    }

    @Test
    void addGame_GamesAlreadyExists_ReturnsConflict() throws Exception {
        GameRequestDto requestDto = new GameRequestDto(12345L, "Halo", 10.0, 15.0, LocalDate.of(2001, 11, 15),
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url1"), List.of("url2"), List.of("PC"));

        doThrow(new dev.abdulrahman.excelsior.presentation.exception.GameAlreadyExistsException("Game with IGDB ID '12345' already exists.")).when(gameService).addAllGames(anyList());

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
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url1"), List.of("url2"), List.of("PC"));

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
        GameResponseDto responseDto = new GameResponseDto(1L, "Halo", 10.0, 15.0, LocalDate.of(2001, 11, 15), "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"));
        when(gameService.findGamesDynamically(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Halo"))
                .andExpect(jsonPath("$[0].length_normal").value(10.0));

        verify(gameService, times(1)).findGamesDynamically(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getGame_GameExists_ReturnsGame() throws Exception {
        GameResponseDto responseDto = new GameResponseDto(1L, "Halo", 10.0, 15.0, LocalDate.of(2001, 11, 15), "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"));
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
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url1"), List.of("url2"), List.of("PC"));

        mockMvc.perform(put("/api/games/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(gameService, times(1)).updateGame(eq(1L), any(GameRequestDto.class));
    }

    @Test
    void updateGame_GameDoesNotExist_ReturnsNotFound() throws Exception {
        GameRequestDto requestDto = new GameRequestDto(12345L, "Halo 2", 12.0, 15.0, LocalDate.of(2004, 11, 9),
                "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Bungie"), GameTier.S, GameStatus.COMPLETED, List.of("url1"), List.of("url2"), List.of("PC"));
        
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
    void searchGame_ValidRequest_ReturnsGameRequestDto() throws Exception {
        GameRequestDto responseDto = new GameRequestDto(12345L, "God of War", 10.0, 15.0, LocalDate.of(2005, 3, 22), "url", 95.0, 100, "syn", "desc", "engine", List.of("Action"), List.of("Santa Monica"), GameTier.S, GameStatus.COMPLETED, List.of("url1"), List.of("url2"), List.of("PC"));
        when(gameService.searchGame("God of War", null)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/games/search")
                .param("query", "God of War")
                .with(request -> { request.setRemoteAddr("1.1.1.1"); return request; })
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("God of War"));

        verify(gameService, times(1)).searchGame("God of War", null);
    }

    @Test
    void searchGame_RateLimitExceeded_ReturnsTooManyRequests() throws Exception {
        for (int i = 0; i < 5; i++) {
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
                .andExpect(content().string("Too many search requests. Please wait a minute and try again."));
        
        verify(gameService, times(5)).searchGame("God of War", null);
    }

    @Test
    void updateTier_ValidRequest_ReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/games/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tier\": \"S\"}"))
                .andExpect(status().isOk());

        verify(gameService, times(1)).updateTier(1L, GameTier.S);
    }
}
