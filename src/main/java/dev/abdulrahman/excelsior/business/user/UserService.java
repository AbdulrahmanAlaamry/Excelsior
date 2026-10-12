package dev.abdulrahman.excelsior.business.user;

import dev.abdulrahman.excelsior.business.user.dto.UserPatchDto;
import dev.abdulrahman.excelsior.business.user.dto.UserResponseDto;
import dev.abdulrahman.excelsior.business.user.dto.UserLoginDto;
import dev.abdulrahman.excelsior.business.user.dto.UserRegistrationDto;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import dev.abdulrahman.excelsior.persistence.UserRepository;
import dev.abdulrahman.excelsior.presentation.exception.UserAlreadyExistsException;
import dev.abdulrahman.excelsior.presentation.exception.UserNotFoundException;
import dev.abdulrahman.excelsior.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DtoMapper mapper;

    public UserService(
            UserRepository repository,
            PasswordEncoder encoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            DtoMapper mapper
    ) {
        this.repository = repository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    public void registerUser(UserRegistrationDto dto) {
        checkUniqueness(dto.username());

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

    public List<UserResponseDto> getAllUsers() {
        return repository.findAll().stream()
                .map(mapper::toUserDto)
                .toList();
    }

    public void deleteUser(Long id) {
        repository.delete(findUserById(id));
        log.info("Deleted user with ID {}", id);
    }

    public void updateUser(Long id, UserPatchDto dto) {
        User user = findUserById(id);
        updateUser(user, dto.username(), dto.password());
    }

    public void updateUser(UserDetails userDetails, UserPatchDto dto) {
        User userEntity = findUserByUsername(userDetails.getUsername());
        updateUser(userEntity, dto.username(), dto.password());
    }

    private void updateUser(User user, String username, String password) {
        if (username == null && password == null) {
            return;
        }

        if (username != null && !username.equals(user.getUsername())) {
            checkUniqueness(username);
            user.setUsername(username);
        }

        if (password != null) {
            user.setPassword(encoder.encode(password));
        }

        repository.save(user);
    }

    public User findUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Could not find user with ID " + id));
    }

    public User findUserByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Could not find user with name " + username));
    }

    public Boolean isUnique(String username) {
        return !repository.existsByUsername(username);
    }

    private void checkUniqueness(String username) {
        if (!isUnique(username)) {
            throw new UserAlreadyExistsException("User with username '" + username + "' already exists");
        }
    }
}
