package dev.abdulrahman.excelsior.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GameDto(
        Integer id,
        ImageDto cover,
        @JsonProperty("first_release_date") Long releaseDate,
        String name,
        String slug,
        String storyline,
        String summary,
        @JsonProperty("total_rating") Double rating,
        @JsonProperty("total_rating_count") Integer ratingCount,
        @JsonProperty("game_engines") EngineDto[] engines,
        GenreDto[] genres,
        @JsonProperty("involved_companies") InvolvedCompanyDto[] involvedCompanies,
        ImageDto[] screenshots,
        PlatformDto[] platforms
) {}
