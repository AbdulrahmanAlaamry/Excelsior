package dev.abdulrahman.excelsior.business.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GameRequestDto(
        String name,
        @JsonProperty("length_normal") Double lengthNormal,
        @JsonProperty("length_complete") Double lengthComplete,
        @JsonFormat(pattern = "dd/MM/yyyy")
        @JsonProperty("release_date")
        LocalDate releaseDate,
        @JsonProperty("cover_url") String coverUrl,
        Double rating,
        @JsonProperty("rating_count") Integer ratingCount,
        String synopsis,
        String description
) {
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return name +
                "\nLength (average): " + lengthNormal +
                "\nLength (100% completion): " + lengthComplete +
                "\nRelease Date: " + releaseDate.format(formatter) +
                "\nCover URL: " + coverUrl +
                "\nRating (" + ratingCount + " ratings): " + rating +
                "\nSynopsis: " + synopsis +
                "\nDescription: " + description;
    }
}
