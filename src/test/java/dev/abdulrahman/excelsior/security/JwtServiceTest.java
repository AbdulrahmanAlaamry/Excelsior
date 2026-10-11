package dev.abdulrahman.excelsior.security;

import dev.abdulrahman.excelsior.business.user.User;
import dev.abdulrahman.excelsior.business.user.UserAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private String secret;

    @BeforeEach
    void setUp() {
        secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = "testSecretKeyForJwtAuthenticationMustBeAtLeast256BitsLongForHmacSha256!";
        }
        long expirationMs = 3600000; // 1 hour
        jwtService = new JwtService(secret, expirationMs);

        User user = new User("abdulrahman", "password");
        userDetails = new UserAdapter(user);
    }

    @Test
    void generateToken_AndExtractUsername_ReturnsCorrectUsername() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("abdulrahman", username);
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_DifferentUser_ReturnsFalse() {
        String token = jwtService.generateToken(userDetails);

        User otherUser = new User("otherUser", "password");
        UserDetails otherDetails = new UserAdapter(otherUser);

        assertFalse(jwtService.isTokenValid(token, otherDetails));
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        long negativeExpiration = -1000; // expired 1s ago
        JwtService expiredJwtService = new JwtService(secret, negativeExpiration);

        String expiredToken = expiredJwtService.generateToken(userDetails);

        assertFalse(expiredJwtService.isTokenValid(expiredToken, userDetails));
    }

    @Test
    void isTokenValid_TamperedToken_ReturnsFalse() {
        String token = jwtService.generateToken(userDetails);
        String tamperedToken = token + "xyz";

        assertFalse(jwtService.isTokenValid(tamperedToken, userDetails));
    }

    @Test
    void isTokenValid_MalformedToken_ReturnsFalse() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token", userDetails));
    }
}
