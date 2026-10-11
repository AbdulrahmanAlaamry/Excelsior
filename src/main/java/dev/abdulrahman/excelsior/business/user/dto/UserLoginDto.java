package dev.abdulrahman.excelsior.business.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDto(
        @NotBlank String username,
        @NotBlank String password
) {
}
