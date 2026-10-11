package dev.abdulrahman.excelsior.mapper;

import dev.abdulrahman.excelsior.business.game.*;
import dev.abdulrahman.excelsior.business.game.dto.GameRequestDto;
import dev.abdulrahman.excelsior.business.game.dto.GameResponseDto;
import dev.abdulrahman.excelsior.business.user.User;
import dev.abdulrahman.excelsior.business.user.dto.UserDto;
import dev.abdulrahman.excelsior.integration.dto.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

@Component
public class DtoMapper {
    public GameResponseDto toGameResponseDto(GameDto gameDto, Boolean isInLibrary, TimeToBeatDto timeToBeatDto) {
        LocalDate releaseDate = gameDto.releaseDate() != null
                ? Instant.ofEpochSecond(gameDto.releaseDate()).atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.EPOCH;

        Double lengthNormal = 0.0;

        Double lengthComplete = 0.0;

        if (timeToBeatDto != null) {
            Integer normally = timeToBeatDto.normally();
            Integer completely = timeToBeatDto.completely();

            if (normally != null) lengthNormal = secondsToHours(normally);
            if (completely != null) lengthComplete = secondsToHours(completely);
        }

        String coverUrl = gameDto.cover() != null
                ? formatImageUrl(gameDto.cover().url())
                : null;

        String primaryEngine = "Unknown/Proprietary";
        EngineDto[] engines = gameDto.engines();
        if (engines != null && engines.length > 0) {
            primaryEngine = engines[0].name();
        }

        List<String> genres = parseArrayToList(gameDto.genres(), GenreDto::name);

        CompanyDto[] companyDtos = parseArrayToArray(gameDto.involvedCompanies(), InvolvedCompanyDto::company, CompanyDto[]::new);
        List<String> companies = parseArrayToList(companyDtos, CompanyDto::name);

        List<String> screenshotUrls = parseArrayToList(gameDto.screenshots(), img -> formatImageUrl(img.url()));
        List<String> platforms = parseArrayToList(gameDto.platforms(), PlatformDto::name);

        return new GameResponseDto(
                null,
                Long.valueOf(gameDto.id()),
                gameDto.name(),
                lengthNormal,
                lengthComplete,
                releaseDate,
                coverUrl,
                round(gameDto.rating()),
                Objects.requireNonNullElse(gameDto.ratingCount(), 0),
                truncate(gameDto.storyline()),
                truncate(gameDto.summary()),
                primaryEngine,
                genres,
                companies,
                GameTier.NONE,
                GameStatus.UNKNOWN,
                screenshotUrls,
                platforms,
                isInLibrary,
                0
        );
    }

    public Game toGameEntity(GameRequestDto dto, User user) {
        return new Game(
                dto.igdbId(),
                dto.name(),
                dto.lengthNormal(),
                dto.lengthComplete(),
                dto.releaseDate(),
                dto.coverUrl(),
                dto.rating(),
                dto.ratingCount(),
                dto.synopsis(),
                dto.description(),
                dto.primaryEngine(),
                dto.genres(),
                dto.companies(),
                dto.tier(),
                dto.status(),
                dto.screenshotUrls(),
                dto.platforms(),
                user
        );
    }

    public GameResponseDto toGameResponseDto(Game game) {
        return new GameResponseDto(
                game.getId(),
                game.getIgdbId(),
                game.getName(),
                game.getLengthNormal(),
                game.getLengthComplete(),
                game.getReleaseDate(),
                game.getCoverUrl(),
                game.getRating(),
                game.getRatingCount(),
                game.getSynopsis(),
                game.getDescription(),
                game.getPrimaryEngine(),
                game.getGenres(),
                game.getCompanies(),
                game.getTierValue(),
                game.getStatus(),
                game.getScreenshotUrls(),
                game.getPlatforms(),
                true,
                game.getTierPosition()
        );
    }

    public List<GameResponseDto> toGameResponseDtoList(List<Game> games) {
        return games.stream()
                .map(this::toGameResponseDto)
                .toList();
    }

    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }

    private Double secondsToHours(Integer seconds) {
        return round(seconds / 3600.0);
    }

    private Double round(Double d) {
        return d != null ? Math.round(d * 10.0) / 10.0 : 0.0;
    }

    public <T, U> T[] parseArrayToArray(U[] arr, Function<U, T> mapper, IntFunction<T[]> arrayGenerator) {
        return arr != null
                ? Arrays.stream(arr).map(mapper).toArray(arrayGenerator)
                : arrayGenerator.apply(0);
    }

    public <T, U> List<T> parseArrayToList(U[] arr, Function<U, T> mapper) {
        return arr != null
                ? Arrays.stream(arr).map(mapper).toList()
                : List.of();
    }

    private String formatImageUrl(String url) {
        if (url == null) {
            return null;
        }

        return url.replace("t_thumb", "t_1080p").replaceFirst("^//", "https://");
    }

    private String truncate(String text) {
        return (text != null && text.length() > 4995)
                ? text.substring(0, 4995) + "..."
                : text;
    }
}
