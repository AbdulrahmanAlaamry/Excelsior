package dev.abdulrahman.excelsior.integration;

import dev.abdulrahman.excelsior.business.game.GameNotFoundException;
import dev.abdulrahman.excelsior.business.game.GameRequestDto;
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

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private DtoMapper mapper;

    private ClientService clientService;
    private MockWebServer mockWebServer;

    private final String clientId = "test-client-id";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String apiUrl = mockWebServer.url("/api/v4").toString();
        clientService = new ClientService(clientId, apiUrl, authService, mapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void searchGames_SuccessfulMatch_ReturnsGameRequestDto() throws Exception {
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

        GameRequestDto expectedDto = new GameRequestDto("The Legend of Zelda", 10.0, 20.0, LocalDate.now(), "http://cover.url", 90.0, 100, "synopsis", "desc");
        when(mapper.toGameRequestDto(any(GameDto.class), any(TimeToBeatDto.class))).thenReturn(expectedDto);

        GameRequestDto result = clientService.searchGames(searchQuery);

        assertEquals(expectedDto, result);
        verify(mapper, times(1)).toGameRequestDto(any(GameDto.class), any(TimeToBeatDto.class));

        var req1 = mockWebServer.takeRequest();
        assertEquals("/api/v4/games", req1.getPath());
        assertEquals("Bearer valid-token", req1.getHeader("Authorization"));
        assertEquals("test-client-id", req1.getHeader("Client-ID"));
        assertTrue(req1.getBody().readUtf8().contains("search \"Zelda\";"));

        var req2 = mockWebServer.takeRequest();
        assertEquals("/api/v4/game_time_to_beats", req2.getPath());
        assertTrue(req2.getBody().readUtf8().contains("where game_id = 100;"));
    }

    @Test
    void searchGames_NoResults_ThrowsGameNotFoundException() throws Exception {
        String searchQuery = "UnknownGame";
        String token = "valid-token";
        
        when(authService.fetchAccessToken()).thenReturn(token);

        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        assertThrows(GameNotFoundException.class, () -> clientService.searchGames(searchQuery));
        verify(mapper, never()).toGameRequestDto(any(), any());
    }

    @Test
    void searchGames_NullResponse_ThrowsGameNotFoundException() {
        String searchQuery = "UnknownGame";
        String token = "valid-token";

        when(authService.fetchAccessToken()).thenReturn(token);

        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        assertThrows(GameNotFoundException.class, () -> clientService.searchGames(searchQuery));
    }

    @Test
    void getTimeToBeat_EmptyResponse_ReturnsZeros() {
        String token = "valid-token";
        when(authService.fetchAccessToken()).thenReturn(token);

        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        TimeToBeatDto result = clientService.getTimeToBeat(999);

        assertNotNull(result);
        assertEquals(0, result.completely());
        assertEquals(0, result.normally());
    }
}
