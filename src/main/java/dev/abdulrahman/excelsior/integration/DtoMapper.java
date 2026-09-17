package dev.abdulrahman.excelsior.integration;

import dev.abdulrahman.excelsior.business.game.GameRequestDto;
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
                gameDto.name(),
                lengthNormal,
                lengthComplete,
                releaseDate,
                gameDto.cover().url(),
                gameDto.rating(),
                gameDto.ratingCount(),
                gameDto.storyline(),
                gameDto.summary()
        );
    }

    private Double secondsToHours(Integer seconds) {
        return seconds / 3600.0;
    }
}
