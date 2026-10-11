package dev.abdulrahman.excelsior;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "igdb.client-id=test",
        "igdb.client-secret=test",
        "igdb.api-url=http://test",
        "igdb.auth-url=http://test",
        "jwt.secret=${JWT_SECRET:testSecretKeyForJwtAuthenticationMustBeAtLeast256BitsLongForHmacSha256!}"
})
class ExcelsiorApplicationTests {

    @Test
    void contextLoads() {
    }
}
