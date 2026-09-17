package dev.abdulrahman.excelsior.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GameDto(
        Integer id,
        CoverDto cover,
        @JsonProperty("first_release_date") Long releaseDate,
        String name,
        String slug,
        String storyline,
        String summary,
        @JsonProperty("total_rating") Double rating,
        @JsonProperty("total_rating_count") Integer ratingCount
) {}
