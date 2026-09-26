package dev.abdulrahman.excelsior.business.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record GameRequestDto(
        @JsonProperty("igdb_id")
        Long igdbId,

        @NotBlank(message = "Game name cannot be empty.")
        String name,

        @NotNull(message = "Normal length must be specified.")
        @Min(value = 0, message = "Length cannot be negative.")
        @JsonProperty("length_normal")
        Double lengthNormal,

        @NotNull(message = "Complete length must be specified.")
        @Min(value = 0, message = "Length cannot be negative.")
        @JsonProperty("length_complete")
        Double lengthComplete,

        @NotNull(message = "Release date must be specified.")
        @JsonFormat(pattern = "dd/MM/yyyy")
        @JsonProperty("release_date")
        LocalDate releaseDate,

        @JsonProperty("cover_url")
        String coverUrl,

        @NotNull(message = "Rating must be specified.")
        @Min(value = 0, message = "Rating cannot be negative.")
        Double rating,

        @NotNull(message = "Rating count must be specified.")
        @Min(value = 0, message = "Rating count cannot be negative.")
        @JsonProperty("rating_count")
        Integer ratingCount,

        String synopsis,
        String description,

        @JsonProperty("primary_engine")
        String primaryEngine,

        List<String> genres,
        List<String> companies,

        @NotNull(message = "Game tier must be specified.")
        GameTier tier,

        @NotNull(message = "Game status must be specified.")
        GameStatus status,

        List<String> artworkUrls,
        List<String> screenshotUrls,
        List<String> platforms
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
                "\nDescription: " + description +
                "\nPrimary Engine: " + primaryEngine +
                "\nTier: " + tier +
                "\nStatus: " + status;
    }
}
