package dev.abdulrahman.excelsior.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.abdulrahman.excelsior.business.GameService;
import dev.abdulrahman.excelsior.business.game.GameNotFoundException;
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

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private GameService gameService;

    @Test
    void addGame_ValidRequest_ReturnsCreated() throws Exception {
        GameRequestDto requestDto = new GameRequestDto("Halo", 10.0, 15.0, LocalDate.of(2001, 11, 15),
                "url", 95.0, 100, "syn", "desc");

        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(gameService, times(1)).addGame(any(GameRequestDto.class));
    }

    @Test
    void getAllGames_ReturnsGameList() throws Exception {
        GameResponseDto responseDto = new GameResponseDto(1L, "Halo", 10.0, LocalDate.of(2001, 11, 15));
        when(gameService.getAllGames()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Halo"))
                .andExpect(jsonPath("$[0].length").value(10.0));

        verify(gameService, times(1)).getAllGames();
    }

    @Test
    void getGame_GameExists_ReturnsGame() throws Exception {
        GameResponseDto responseDto = new GameResponseDto(1L, "Halo", 10.0, LocalDate.of(2001, 11, 15));
        when(gameService.getGameById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/games/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Halo"));

        verify(gameService, times(1)).getGameById(1L);
    }

    @Test
    void getGame_GameDoesNotExist_ReturnsNotFound() throws Exception {
        when(gameService.getGameById(99L)).thenThrow(new GameNotFoundException());

        mockMvc.perform(get("/api/games/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(gameService, times(1)).getGameById(99L);
    }

    @Test
    void updateGame_ValidRequest_ReturnsOk() throws Exception {
        GameRequestDto requestDto = new GameRequestDto("Halo 2", 12.0, 15.0, LocalDate.of(2004, 11, 9),
                "url", 95.0, 100, "syn", "desc");

        mockMvc.perform(put("/api/games/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(gameService, times(1)).updateGame(eq(1L), any(GameRequestDto.class));
    }

    @Test
    void updateGame_GameDoesNotExist_ReturnsNotFound() throws Exception {
        GameRequestDto requestDto = new GameRequestDto("Halo 2", 12.0, 15.0, LocalDate.of(2004, 11, 9),
                "url", 95.0, 100, "syn", "desc");
        
        doThrow(new GameNotFoundException()).when(gameService).updateGame(eq(99L), any(GameRequestDto.class));

        mockMvc.perform(put("/api/games/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

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
        doThrow(new GameNotFoundException()).when(gameService).deleteGame(99L);

        mockMvc.perform(delete("/api/games/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(gameService, times(1)).deleteGame(99L);
    }
}
