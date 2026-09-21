package dev.abdulrahman.excelsior.mapper;

import dev.abdulrahman.excelsior.business.game.Game;
import dev.abdulrahman.excelsior.business.game.GameRequestDto;
import dev.abdulrahman.excelsior.business.game.GameResponseDto;
import dev.abdulrahman.excelsior.integration.dto.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;

@Component
public class DtoMapper {
    public GameRequestDto toGameRequestDto(GameDto gameDto, TimeToBeatDto timeToBeatDto) {
        LocalDate releaseDate = gameDto.releaseDate() != null
                ? Instant.ofEpochSecond(gameDto.releaseDate()).atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.EPOCH;

        Double lengthNormal = timeToBeatDto.normally() != null
                ? secondsToHours(timeToBeatDto.normally())
                : 0.0;

        Double lengthComplete = timeToBeatDto.completely() != null
                ? secondsToHours(timeToBeatDto.completely())
                : 0.0;

        String primaryEngine = "Unknown/Proprietary";
        EngineDto[] engines = gameDto.engines();
        if (engines != null && engines.length > 0) {
            primaryEngine = engines[0].name();
        }

        String[] genres = parseArray(gameDto.genres(), GenreDto::name, String[]::new);

        CompanyDto[] companyDtos = parseArray(gameDto.involvedCompanies(), InvolvedCompanyDto::company, CompanyDto[]::new);
        String[] companies = parseArray(companyDtos, CompanyDto::name, String[]::new);

        return new GameRequestDto(
                Long.valueOf(gameDto.id()),
                gameDto.name(),
                lengthNormal,
                lengthComplete,
                releaseDate,
                gameDto.cover().url(),
                round(gameDto.rating()),
                gameDto.ratingCount(),
                gameDto.storyline(),
                gameDto.summary(),
                primaryEngine,
                genres,
                companies
        );
    }

    public Game toGameEntity(GameRequestDto dto) {
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
                dto.companies()
        );
    }

    public GameResponseDto toGameResponseDto(Game game) {
        return new GameResponseDto(
                game.getId(),
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
                game.getCompanies()
        );
    }

    private Double secondsToHours(Integer seconds) {
        return round(seconds / 3600.0);
    }

    private Double round(Double d) {
        return d != null ? Math.round(d * 10.0) / 10.0 : null;
    }

    public <T, U> T[] parseArray(U[] arr, Function<U, T> mapper, IntFunction<T[]> arrayGenerator) {
        return arr != null
                ? Arrays.stream(arr).map(mapper).toArray(arrayGenerator)
                : arrayGenerator.apply(0);
    }
}
