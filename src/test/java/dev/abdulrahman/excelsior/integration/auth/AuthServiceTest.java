package dev.abdulrahman.excelsior.integration.auth;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        String authUrl = mockWebServer.url("/auth").toString();
        authService = new AuthService("test-client-id", "test-client-secret", authUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchAccessToken_SuccessfulResponse_ReturnsToken() throws Exception {
        String expectedToken = "valid-access-token";
        String responseBody = """
                {
                    "access_token": "valid-access-token",
                    "expires_in": 3600,
                    "token_type": "bearer"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        String token = authService.fetchAccessToken();

        assertEquals(expectedToken, token);
        
        var request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/auth?client_id=test-client-id&client_secret=test-client-secret&grant_type=client_credentials", request.getPath());
    }

    @Test
    void fetchAccessToken_NullResponse_ThrowsException() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        assertThrows(ApiAuthenticationException.class, () -> authService.fetchAccessToken());
    }
    
    @Test
    void fetchAccessToken_NullTokenInResponse_ThrowsException() {
        String responseBody = """
                {
                    "expires_in": 3600,
                    "token_type": "bearer"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        assertThrows(ApiAuthenticationException.class, () -> authService.fetchAccessToken());
    }
}
