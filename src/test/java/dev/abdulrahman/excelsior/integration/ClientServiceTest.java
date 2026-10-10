package dev.abdulrahman.excelsior.integration;

import dev.abdulrahman.excelsior.persistence.GameRepository;
import dev.abdulrahman.excelsior.business.user.User;
import dev.abdulrahman.excelsior.presentation.exception.GameNotFoundException;
import dev.abdulrahman.excelsior.business.game.dto.GameResponseDto;
import dev.abdulrahman.excelsior.integration.auth.AuthService;
import dev.abdulrahman.excelsior.integration.dto.GameDto;
import dev.abdulrahman.excelsior.integration.dto.TimeToBeatDto;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.abdulrahman.excelsior.business.game.GameTier;
import dev.abdulrahman.excelsior.business.game.GameStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private DtoMapper mapper;

    private User testUser = new User("test", "pass");

    @Mock
    private GameRepository repository;

    private ClientService clientService;
    private MockWebServer mockWebServer;

    private final String clientId = "test-client-id";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String apiUrl = mockWebServer.url("/api/v4").toString();
        clientService = new ClientService(clientId, apiUrl, authService, mapper, repository);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void searchGames_SuccessfulMatch_ReturnsGameResponseDto() throws Exception {
        String searchQuery = "Zelda";
        String token = "valid-token";
        
        when(authService.fetchAccessToken()).thenReturn(token);

        String gameResponseBody = """
                [
                    {
                        "id": 100,
                        "name": "The Legend of Zelda",
                        "slug": "legend-of-zelda",
                        "cover": {
                            "url": "http://cover.url"
                        }
                    }
                ]
                """;

        String ttbResponseBody = """
                [
                    {
                        "game_id": 100,
                        "completely": 72000,
                        "normally": 36000
                    }
                ]
                """;

        // Enqueue game search response
        mockWebServer.enqueue(new MockResponse()
                .setBody(gameResponseBody)
                .addHeader("Content-Type", "application/json"));

        // Enqueue time to beat response
        mockWebServer.enqueue(new MockResponse()
                .setBody(ttbResponseBody)
                .addHeader("Content-Type", "application/json"));

        when(repository.existsByUserAndIgdbId(testUser, 100L)).thenReturn(false);

        GameResponseDto expectedDto = new GameResponseDto(1L, 100L, "Awesome Game", 10.0, 20.0,
                LocalDate.of(2023, 10, 15), "http://example.com/cover.jpg",
                85.5, 120, "A great storyline", "A short summary", "engine", List.of("Action"), List.of("Sony"), GameTier.S, GameStatus.COMPLETED, List.of("url2"), List.of("PC"), false, 0);
        when(mapper.toGameResponseDto(any(GameDto.class), eq(false), any(TimeToBeatDto.class))).thenReturn(expectedDto);

        List<GameResponseDto> result = clientService.searchGames(searchQuery, 1, 0, testUser);

        assertEquals(List.of(expectedDto), result);
        verify(mapper, times(1)).toGameResponseDto(any(GameDto.class), eq(false), any(TimeToBeatDto.class));
        verify(repository, times(1)).existsByUserAndIgdbId(testUser, 100L);

        var req1 = mockWebServer.takeRequest();
        assertEquals("/api/v4/games", req1.getPath());
        assertEquals("Bearer valid-token", req1.getHeader("Authorization"));
        assertEquals("test-client-id", req1.getHeader("Client-ID"));
        assertTrue(req1.getBody().readUtf8().contains("search \"Zelda\";"));

        var req2 = mockWebServer.takeRequest();
        assertEquals("/api/v4/game_time_to_beats", req2.getPath());
        assertTrue(req2.getBody().readUtf8().contains("where game_id = (100);"));
    }

    @Test
    void searchGames_NoResults_ThrowsGameNotFoundException() throws Exception {
        String searchQuery = "UnknownGame";
        String token = "valid-token";
        
        when(authService.fetchAccessToken()).thenReturn(token);

        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        assertThrows(GameNotFoundException.class, () -> clientService.searchGames(searchQuery, 10, 0, testUser));
        verify(mapper, never()).toGameResponseDto(any(), anyBoolean(), any());
    }

    @Test
    void searchGames_NullResponse_ThrowsGameNotFoundException() {
        String searchQuery = "UnknownGame";
        String token = "valid-token";

        when(authService.fetchAccessToken()).thenReturn(token);

        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        assertThrows(GameNotFoundException.class, () -> clientService.searchGames(searchQuery, 10, 0, testUser));
    }

    @Test
    void getTimeToBeat_EmptyResponse_ReturnsZeros() {
        String token = "valid-token";
        when(authService.fetchAccessToken()).thenReturn(token);

        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        Map<Integer, TimeToBeatDto> result = clientService.getTimeToBeat(java.util.Set.of(999));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
