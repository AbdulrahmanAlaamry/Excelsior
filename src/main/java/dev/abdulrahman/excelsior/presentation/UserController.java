package dev.abdulrahman.excelsior.presentation;

import dev.abdulrahman.excelsior.business.user.UserService;
import dev.abdulrahman.excelsior.business.user.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/users")
public class UserController {
    private static final String INVALID_ID_MESSAGE = "User ID must be greater than 0.";
    private static final String NULL_ID_MESSAGE = "User ID cannot be null.";

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(
            @Valid
            @RequestBody
            UserRegistrationDto dto
    ) {
        service.registerUser(dto);
    }

    @PostMapping("/login")
    public JwtResponseDto loginUser(
            @Valid
            @RequestBody
            UserLoginDto dto
    ) {
        String token = service.loginUser(dto);
        return new JwtResponseDto(token);
    }

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return service.getAllUsers();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @NotNull(message = NULL_ID_MESSAGE)
            @Positive(message = INVALID_ID_MESSAGE)
            @PathVariable
            Long id) {
        service.deleteUser(id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(
            @NotNull(message = NULL_ID_MESSAGE)
            @Positive(message = INVALID_ID_MESSAGE)
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UserPatchDto dto
    ) {
        // Accessible only by admins, no need for UserDetails injection
        service.updateUser(id, dto);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(
            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @RequestBody
            UserPatchDto dto
    ) {
        service.updateUser(userDetails, dto);
    }

    @GetMapping("/check-username")
    public Map<String, Boolean> checkUsernameAvailability(
            @NotBlank(message = "Username cannot be blank.")
            @RequestParam
            String username
    ) {
        return Map.of("available", service.isUnique(username));
    }
}
