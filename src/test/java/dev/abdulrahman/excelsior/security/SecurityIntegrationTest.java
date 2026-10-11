package dev.abdulrahman.excelsior.security;

import dev.abdulrahman.excelsior.business.game.GameService;
import dev.abdulrahman.excelsior.business.user.User;
import dev.abdulrahman.excelsior.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "igdb.client-id=test",
        "igdb.client-secret=test",
        "igdb.api-url=http://test",
        "igdb.auth-url=http://test",
        "jwt.secret=${JWT_SECRET:testSecretKeyForJwtAuthenticationMustBeAtLeast256BitsLongForHmacSha256!}"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private GameService gameService;

    @Test
    void protectedEndpoint_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicSearchEndpoint_WithoutToken_ReturnsOk() throws Exception {
        when(gameService.searchGame("Halo", null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/games/search").param("query", "Halo"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_WithValidToken_ReturnsOk() throws Exception {
        String username = "jwtUser_" + System.currentTimeMillis();
        User user = userRepository.save(new User(username, passwordEncoder.encode("secret")));
        String token = jwtService.generateToken(user.getUsername());
        when(gameService.findGamesDynamically(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/games")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void publicRegisterEndpoint_WithoutToken_IsAccessible() throws Exception {
        String registerUsername = "regUser_" + System.currentTimeMillis();
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + registerUsername + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isCreated());
    }
}
