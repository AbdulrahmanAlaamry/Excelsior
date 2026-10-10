package dev.abdulrahman.excelsior.business.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegistrationDto(
        @NotNull
        @Size(min = 6, max = 32) // TODO: Tweak minimum/maximum username lengths
        String username,

        @NotNull
        @Size(min = 8, max = 32) // TODO: Tweak password validation and minimum/maximum lengths
        String password
) {
}
