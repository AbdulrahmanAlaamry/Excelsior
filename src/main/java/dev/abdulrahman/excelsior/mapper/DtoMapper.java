package dev.abdulrahman.excelsior.mapper;

import dev.abdulrahman.excelsior.business.game.Game;
import dev.abdulrahman.excelsior.business.game.GameRequestDto;
import dev.abdulrahman.excelsior.business.game.GameResponseDto;
import dev.abdulrahman.excelsior.integration.dto.GameDto;
import dev.abdulrahman.excelsior.integration.dto.TimeToBeatDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

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
                gameDto.summary()
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
                dto.description()
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
                game.getDescription()
        );
    }

    private Double secondsToHours(Integer seconds) {
        return round(seconds / 3600.0);
    }

    private Double round(Double d) {
        return Math.round(d * 10.0) / 10.0;
    }
}
