package dev.abdulrahman.excelsior.business.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GameFilterDto(
        @Min(value = 0, message = "Minimum rating cannot be negative.")
        Double minRating,

        @Min(value = 0, message = "Maximum rating cannot be negative.")
        Double maxRating,

        @Size(min = 1, message = "'name_contains' parameter must contain at least 1 character.")
        String nameContains,

        @Size(min = 1, message = "'genre_contains' parameter must contain at least 1 character.")
        String genreContains,

        @Min(value = 0, message = "Minimum normal length cannot be negative.")
        Double minLengthNormal,

        @Min(value = 0, message = "Maximum normal length cannot be negative.")
        Double maxLengthNormal,

        @Min(value = 0, message = "Minimum complete length cannot be negative.")
        Double minLengthComplete,

        @Min(value = 0, message = "Maximum complete length cannot be negative.")
        Double maxLengthComplete,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate minDate,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate maxDate,

        @Min(value = 0, message = "Minimum release year cannot be negative.")
        Integer minYear,

        @Min(value = 0, message = "Maximum release year cannot be negative.")
        Integer maxYear,

        GameTier tier,

        GameStatus status,

        @Size(min = 1, message = "'platform_contains' parameter must contain at least 1 character.")
        String platformContains
) {
}
