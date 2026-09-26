package dev.abdulrahman.excelsior.integration;

import dev.abdulrahman.excelsior.presentation.exception.GameNotFoundException;
import dev.abdulrahman.excelsior.business.game.GameRequestDto;
import dev.abdulrahman.excelsior.integration.auth.AuthService;
import dev.abdulrahman.excelsior.integration.dto.GameDto;
import dev.abdulrahman.excelsior.integration.dto.TimeToBeatDto;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
public class ClientService {
    private static final Integer DEFAULT_API_QUERY_LIMIT = 5;
    private final String clientId;
    private final String apiUrl;
    private final AuthService authService;
    private final DtoMapper mapper;
    private final RestClient restClient;
    private String cachedToken = null;

    public ClientService(
            @Value("${igdb.client-id}") String clientId,
            @Value("${igdb.api-url}") String apiUrl,
            AuthService authService,
            DtoMapper mapper
    ) {
        this.clientId = clientId;
        this.apiUrl = apiUrl;
        this.authService = authService;
        this.mapper = mapper;
        this.restClient = RestClient.create();
    }

    public List<GameRequestDto> searchGames(String query, Integer limit) {
        if (limit == null) {
            limit = DEFAULT_API_QUERY_LIMIT;
        }

        GameDto[] gameResponse = initiateRequest(
                "/games",
                "fields id, cover.url, cover.width, cover.height, " +
                        "first_release_date, name, slug, storyline, summary, total_rating, total_rating_count, " +
                        "game_engines.name, genres.name, involved_companies.company.name, " +
                        "screenshots.url, screenshots.width, screenshots.height, platforms.name; " +
                        "search \"" + query + "\";" +
                        "where game_type = (0, 8, 9) & version_parent = null;" +
                        "limit " + limit + ";",
                GameDto[].class
        );

        if (gameResponse == null || gameResponse.length == 0) {
            throw new GameNotFoundException("Could not find a game with the query \"" + query + "\"");
        }

        return Arrays.stream(gameResponse)
                .map(gameDto -> {
                    TimeToBeatDto timeToBeatDto = getTimeToBeat(gameDto.id());
                    return mapper.toGameRequestDto(gameDto, timeToBeatDto);
                })
                .toList();
    }

    public TimeToBeatDto getTimeToBeat(Integer igdbId) {
        TimeToBeatDto[] response = initiateRequest(
                "/game_time_to_beats",
                "fields completely, normally; where game_id = " + igdbId + ";",
                TimeToBeatDto[].class
        );

        return (response != null && response.length > 0)
                ? response[0]
                : new TimeToBeatDto(0, 0);
    }

    private <T> T initiateRequest(String endpoint, String body, Class<T> responseType) {
        return restClient.post()
                .uri(apiUrl + endpoint)
                .header("Client-ID", clientId)
                .header("Authorization", "Bearer " + getAccessToken())
                .body(body)
                .retrieve()
                .body(responseType);
    }

    private String getAccessToken() {
        if (cachedToken == null) {
            cachedToken = authService.fetchAccessToken();
        }
        return cachedToken;
    }
}
