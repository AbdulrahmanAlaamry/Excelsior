package dev.abdulrahman.excelsior.business.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UserRegistrationDto(
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 32, message = "Username must be between 3 and 32 characters long")
        @Pattern(
                regexp = "^[a-zA-Z0-9_-]+$",
                message = "Username can only contain letters, numbers underscores and hyphens"
        )
        String username,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
        )
        String password
) {
}
