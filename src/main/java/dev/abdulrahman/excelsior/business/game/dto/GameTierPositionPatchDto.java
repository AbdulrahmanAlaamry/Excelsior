package dev.abdulrahman.excelsior.business.game.dto;

import dev.abdulrahman.excelsior.business.game.GameTier;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record GameTierPositionPatchDto(
        @Positive Long id,
        GameTier tier,
        @Min(value = 0, message = "Tier position cannot be negative.") Integer position
) {
}
