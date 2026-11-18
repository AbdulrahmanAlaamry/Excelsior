package dev.abdulrahman.excelsior.integration.auth;

import dev.abdulrahman.excelsior.persistence.OAuthTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TwitchTokenServiceIntegrationTest {

    @Autowired
    private TwitchTokenService tokenService;

    @Autowired
    private OAuthTokenRepository tokenRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private AuthService authService;

    @Test
    void getAccessToken_CalledInsideReadOnlyTransaction_StillPersistsAndSurvivesRestart() {
        tokenRepository.deleteAll();
        tokenService.invalidateToken("persisted-jwt-token");
        when(authService.fetchTokenDetails()).thenReturn(new AuthResponse("persisted-jwt-token", 5000000, "bearer"));

        // Simulates GameService.searchGame() calling TwitchTokenService inside @Transactional(readOnly = true)
        TransactionTemplate readOnlyCallerTx = new TransactionTemplate(transactionManager);
        readOnlyCallerTx.setReadOnly(true);

        String firstToken = readOnlyCallerTx.execute(status -> tokenService.getAccessToken());
        assertEquals("persisted-jwt-token", firstToken);
        verify(authService, times(1)).fetchTokenDetails();

        // Simulate full application restart with empty in-memory L1 cache
        TwitchTokenService restartedService = new TwitchTokenService(authService, tokenRepository, transactionManager);

        String tokenAfterRestart = restartedService.getAccessToken();
        assertEquals("persisted-jwt-token", tokenAfterRestart);
        verifyNoMoreInteractions(authService);
    }
}
