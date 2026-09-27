package dev.abdulrahman.excelsior.business.game;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record GameTierPositionPatchDto(
        @Positive Long id,
        GameTier tier,
        @Min(value = 0, message = "Tier position cannot be negative.") Integer position
) {
}
