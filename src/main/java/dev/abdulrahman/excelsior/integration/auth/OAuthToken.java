package dev.abdulrahman.excelsior.integration.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Persists upstream OAuth2 access tokens across restarts so tokens are only re-requested upon expiry.
 */
@Entity
@Table(name = "oauth_token")
public class OAuthToken {

    @Id
    private String provider;

    @Column(nullable = false, length = 512)
    private String accessToken;

    @Column(nullable = false)
    private Instant expiresAt;

    protected OAuthToken() {}

    public OAuthToken(String provider, String accessToken, Instant expiresAt) {
        this.provider = provider;
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
    }

    public String getProvider() {
        return provider;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
