package dev.abdulrahman.excelsior.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.abdulrahman.excelsior.business.user.UserService;
import dev.abdulrahman.excelsior.business.user.dto.UserLoginDto;
import dev.abdulrahman.excelsior.business.user.dto.UserRegistrationDto;
import dev.abdulrahman.excelsior.presentation.config.RateLimitInterceptor;
import dev.abdulrahman.excelsior.presentation.config.WebConfig;
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

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @Test
    void registerUser_ValidRequest_ReturnsOk() throws Exception {
        UserRegistrationDto request = new UserRegistrationDto("newuser", "password123");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

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
    void deleteUser_ValidId_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(userService, never()).deleteUser(any());
    }
}
