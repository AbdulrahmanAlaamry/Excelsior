package dev.abdulrahman.excelsior.business.user;

import dev.abdulrahman.excelsior.business.user.dto.UserRegistrationDto;
import dev.abdulrahman.excelsior.persistence.UserRepository;
import dev.abdulrahman.excelsior.presentation.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public void registerUser(UserRegistrationDto dto) {
        // Not using DtoMapper for security and SRP reasons
        repository.save(
                new User(
                        dto.username(),
                        encoder.encode(dto.password())
                )
        );
    }

    public void deleteUser(Long id) {
        repository.delete(findUserById(id));
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
