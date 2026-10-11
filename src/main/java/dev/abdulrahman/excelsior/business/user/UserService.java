package dev.abdulrahman.excelsior.business.user;

import dev.abdulrahman.excelsior.business.user.dto.UserLoginDto;
import dev.abdulrahman.excelsior.business.user.dto.UserRegistrationDto;
import dev.abdulrahman.excelsior.persistence.UserRepository;
import dev.abdulrahman.excelsior.presentation.exception.UserNotFoundException;
import dev.abdulrahman.excelsior.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(
            UserRepository repository,
            PasswordEncoder encoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.repository = repository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public void registerUser(UserRegistrationDto dto) {
        // Not using DtoMapper for security and SRP reasons
        repository.save(
                new User(
                        dto.username(),
                        encoder.encode(dto.password())
                )
        );
        log.info("Registered new user: '{}'", dto.username());
    }

    public String loginUser(UserLoginDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );
        String token = jwtService.generateToken(dto.username());
        log.info("User '{}' authenticated successfully (JWT issued)", dto.username());
        return token;
    }

    public void deleteUser(Long id) {
        repository.delete(findUserById(id));
        log.info("Deleted user with ID {}", id);
    }

    public User findUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Could not find user with ID " + id));
    }

    public User findUserByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Could not find user with name " + username));
    }
}
