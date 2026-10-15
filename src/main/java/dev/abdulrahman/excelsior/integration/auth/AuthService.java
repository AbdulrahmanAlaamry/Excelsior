package dev.abdulrahman.excelsior.integration.auth;

import dev.abdulrahman.excelsior.presentation.exception.TwitchAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
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

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public synchronized String fetchAccessToken() {
        log.info("Requesting new Twitch OAuth2 access token...");

        AuthResponse response = restClient.post()
                .uri(authUrl + "?client_id={id}&client_secret={secret}&grant_type=client_credentials",
                        clientId, clientSecret)
                .retrieve()
                .body(AuthResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new TwitchAuthenticationException("Twitch OAuth2 did not return an access token");
        }

        log.info("Twitch OAuth2 access token successfully acquired");
        return response.accessToken();
    }
}
