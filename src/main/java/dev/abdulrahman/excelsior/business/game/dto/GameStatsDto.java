package dev.abdulrahman.excelsior.business.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.abdulrahman.excelsior.business.game.GameStatus;
import dev.abdulrahman.excelsior.business.game.GameTier;

import java.util.Map;

public record GameStatsDto(
        @JsonProperty("total_games") long totalGames,
        @JsonProperty("games_by_status") Map<GameStatus, Long> gamesByStatus,
        @JsonProperty("games_by_tier") Map<GameTier, Long> gamesByTier
) {
}
