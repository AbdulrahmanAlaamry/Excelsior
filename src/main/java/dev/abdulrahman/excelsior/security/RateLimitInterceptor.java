package dev.abdulrahman.excelsior.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private enum RateLimitTier {
        AUTH(5, Duration.ofMinutes(1), "Too many authentication attempts. Please wait a minute before trying again."),
        GENERAL(20, Duration.ofMinutes(1), "You are searching too fast. Please wait a few seconds before trying again.");

        private final int capacity;
        private final Duration refillDuration;
        private final String errorMessage;

        RateLimitTier(int capacity, Duration refillDuration, String errorMessage) {
            this.capacity = capacity;
            this.refillDuration = refillDuration;
            this.errorMessage = errorMessage;
        }

        public Bucket createBucket() {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(capacity)
                    .refillGreedy(capacity, refillDuration)
                    .build();

            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private RateLimitTier resolveTier(String uri) {
        if (uri.startsWith("/api/users/login")) {
            return RateLimitTier.AUTH;
        }
        return RateLimitTier.GENERAL;
    }

    public void reset() {
        buckets.clear();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ipAddress = extractClientIp(request);
        RateLimitTier tier = resolveTier(request.getRequestURI());
        String bucketKey = ipAddress + ":" + tier.name();

        Bucket bucket = buckets.computeIfAbsent(bucketKey, key -> tier.createBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return true;
        }

        long waitForRefillNanos = probe.getNanosToWaitForRefill();
        long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(waitForRefillNanos));

        log.warn("Rate limit triggered for IP '{}' on URI '{}' (tier: {})", ipAddress, request.getRequestURI(), tier);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.getWriter().write(String.format(
                "{\"title\": \"Too Many Requests\", \"detail\": \"%s\"}",
                tier.getErrorMessage()
        ));
        return false;
    }
}

