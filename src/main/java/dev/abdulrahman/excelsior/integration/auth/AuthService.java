package dev.abdulrahman.excelsior.integration.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthService {
    private final String clientId;
    private final String clientSecret;
    private final String authUrl;
    private final RestClient restClient;

    public AuthService(
            @Value("${igdb.client-id}") String clientId,
            @Value("${igdb.client-secret}") String clientSecret,
            @Value("${igdb.auth-url}") String authUrl
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authUrl = authUrl;
        this.restClient = RestClient.create();
    }

    public String fetchAccessToken() {
        AuthResponse response = restClient.post()
                .uri(authUrl + "?client_id={id}&client_secret={secret}&grant_type=client_credentials",
                        clientId, clientSecret)
                .retrieve()
                .body(AuthResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new ApiAuthenticationException();
        }

        return response.accessToken();
    }
}
