package dev.abdulrahman.excelsior.business.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record GameResponseDto(
        Long id,
        @JsonProperty("igdb_id") Long igdbId,
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
        String description,
        @JsonProperty("primary_engine") String primaryEngine,
        List<String> genres,
        List<String> companies,
        GameTier tier,
        GameStatus status,
        List<String> screenshotUrls,
        List<String> platforms,
        @JsonProperty("is_in_library") Boolean isInLibrary
) {
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return name + " (ID: " + id + ")" +
                "\nLength (average): " + lengthNormal +
                "\nLength (100% completion): " + lengthComplete +
                "\nRelease Date: " + releaseDate.format(formatter) +
                "\nCover URL: " + coverUrl +
                "\nRating (" + ratingCount + " ratings): " + rating +
                "\nSynopsis: " + synopsis +
                "\nDescription: " + description +
                "\nPrimary Engine: " + primaryEngine +
                "\nTier: " + tier +
                "\nStatus: " + status +
                "\nIn Library: " + isInLibrary;
    }
}
