package dev.abdulrahman.excelsior.presentation;

import dev.abdulrahman.excelsior.business.user.UserService;
import dev.abdulrahman.excelsior.business.user.dto.JwtResponseDto;
import dev.abdulrahman.excelsior.business.user.dto.UserLoginDto;
import dev.abdulrahman.excelsior.business.user.dto.UserRegistrationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@Valid @RequestBody UserRegistrationDto dto) {
        service.registerUser(dto);
    }

    @PostMapping("/login")
    public JwtResponseDto loginUser(@Valid @RequestBody UserLoginDto dto) {
        String token = service.loginUser(dto);
        return new JwtResponseDto(token);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@NotNull @Positive @PathVariable Long id) {
        service.deleteUser(id);
    }
}
