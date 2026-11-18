package dev.abdulrahman.excelsior.persistence;

import dev.abdulrahman.excelsior.integration.auth.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, String> {
}
