package dev.abdulrahman.excelsior.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.abdulrahman.excelsior.business.user.UserService;
import dev.abdulrahman.excelsior.business.user.dto.UserLoginDto;
import dev.abdulrahman.excelsior.business.user.dto.UserRegistrationDto;
import dev.abdulrahman.excelsior.security.RateLimitInterceptor;
import dev.abdulrahman.excelsior.configuration.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({WebConfig.class, RateLimitInterceptor.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        rateLimitInterceptor.reset();
    }

    @Test
    void registerUser_ValidRequest_ReturnsCreated() throws Exception {
        UserRegistrationDto request = new UserRegistrationDto("newuser", "Password123");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(userService, times(1)).registerUser(request);
    }

    @Test
    void registerUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserRegistrationDto request = new UserRegistrationDto("", "pass");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_InvalidPasswordPattern_ReturnsBadRequest() throws Exception {
        UserRegistrationDto request = new UserRegistrationDto("newuser", "passwordwithoutupper1");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Password must contain at least one uppercase letter, one lowercase letter, and one number"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_InvalidUsernameCharacters_ReturnsBadRequest() throws Exception {
        UserRegistrationDto request = new UserRegistrationDto("invalid user!", "Password123");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Username can only contain letters, numbers underscores and hyphens"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void loginUser_ValidRequest_ReturnsOk() throws Exception {
        UserLoginDto request = new UserLoginDto("validuser", "password123");
        when(userService.loginUser(request)).thenReturn("jwt.token.here");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"));

        verify(userService, times(1)).loginUser(request);
    }

    @Test
    void loginUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserLoginDto request = new UserLoginDto("", "");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(any());
    }

    @Test
    void loginUser_BadCredentials_ReturnsUnauthorized() throws Exception {
        UserLoginDto request = new UserLoginDto("validuser", "wrongpassword");
        when(userService.loginUser(request)).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid username or password"))
                .andExpect(jsonPath("$.status").value(401));

        verify(userService, times(1)).loginUser(request);
    }

    @Test
    void deleteUser_ValidId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(userService, never()).deleteUser(any());
    }

    @Test
    void loginUser_RateLimitExceeded_ReturnsTooManyRequests() throws Exception {
        UserLoginDto request = new UserLoginDto("validuser", "password123");
        when(userService.loginUser(request)).thenReturn("jwt.token.here");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/users/login")
                    .with(req -> { req.setRemoteAddr("198.51.100.1"); return req; })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/users/login")
                .with(req -> { req.setRemoteAddr("198.51.100.1"); return req; })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.title").value("Too Many Requests"))
                .andExpect(jsonPath("$.detail").value("Too many authentication attempts. Please wait a minute before trying again."));

        verify(userService, times(5)).loginUser(request);
    }

    @Test
    void loginUser_WithXForwardedForHeader_UsesForwardedIp() throws Exception {
        UserLoginDto request = new UserLoginDto("validuser", "password123");
        when(userService.loginUser(request)).thenReturn("jwt.token.here");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/users/login")
                    .header("X-Forwarded-For", "203.0.113.195, 10.0.0.1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/users/login")
                .header("X-Forwarded-For", "203.0.113.195, 10.0.0.1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.detail").value("Too many authentication attempts. Please wait a minute before trying again."));
    }
}
