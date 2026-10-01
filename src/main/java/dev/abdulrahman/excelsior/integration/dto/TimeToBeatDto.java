package dev.abdulrahman.excelsior.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimeToBeatDto(
        @JsonProperty("game_id") Integer id,
        Integer completely,
        Integer normally
) {}
