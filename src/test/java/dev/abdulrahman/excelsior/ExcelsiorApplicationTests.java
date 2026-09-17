package dev.abdulrahman.excelsior;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(properties = {
        "igdb.client-id=test",
        "igdb.client-secret=test",
        "igdb.api-url=http://test",
        "igdb.auth-url=http://test"
})
class ExcelsiorApplicationTests {

    @Test
    void contextLoads() {
    }

}
