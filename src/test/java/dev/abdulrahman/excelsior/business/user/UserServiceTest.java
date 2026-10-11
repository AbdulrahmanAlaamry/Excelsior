package dev.abdulrahman.excelsior.business.user;

import dev.abdulrahman.excelsior.business.user.dto.UserLoginDto;
import dev.abdulrahman.excelsior.business.user.dto.UserRegistrationDto;
import dev.abdulrahman.excelsior.persistence.UserRepository;
import dev.abdulrahman.excelsior.presentation.exception.UserNotFoundException;
import dev.abdulrahman.excelsior.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "encodedPassword");
    }

    @Test
    void registerUser_EncodesPasswordAndSaves() {
        UserRegistrationDto dto = new UserRegistrationDto("testuser", "rawPassword");
        when(encoder.encode("rawPassword")).thenReturn("encodedPassword");

        userService.registerUser(dto);

        verify(repository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
    }

    @Test
    void loginUser_ValidCredentials_ReturnsToken() {
        UserLoginDto dto = new UserLoginDto("testuser", "rawPassword");
        when(jwtService.generateToken("testuser")).thenReturn("mocked-jwt-token");

        String token = userService.loginUser(dto);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("mocked-jwt-token", token);
    }

    @Test
    void loginUser_InvalidCredentials_ThrowsException() {
        UserLoginDto dto = new UserLoginDto("testuser", "wrongPassword");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> userService.loginUser(dto));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void deleteUser_UserExists_DeletesUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(repository).delete(user);
    }

    @Test
    void deleteUser_UserDoesNotExist_ThrowsException() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(2L));
        verify(repository, never()).delete(any());
    }

    @Test
    void findUserById_UserExists_ReturnsUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findUserById_UserDoesNotExist_ThrowsException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserById(1L));
    }

    @Test
    void findUserByUsername_UserExists_ReturnsUser() {
        when(repository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.findUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findUserByUsername_UserDoesNotExist_ThrowsException() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserByUsername("unknown"));
    }
}
