package dev.abdulrahman.excelsior.business.game;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record GameStatsDto(
        @JsonProperty("total_games") long totalGames,
        @JsonProperty("games_by_status") Map<GameStatus, Long> gamesByStatus,
        @JsonProperty("games_by_tier") Map<GameTier, Long> gamesByTier
) {
}
