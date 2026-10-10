package dev.abdulrahman.excelsior.business.game.dto;

import dev.abdulrahman.excelsior.business.game.GameStatus;
import dev.abdulrahman.excelsior.business.game.GameTier;

public record GamePatchDto(
        GameTier tier,
        GameStatus status
) {
}
