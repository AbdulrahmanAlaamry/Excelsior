package dev.abdulrahman.excelsior.presentation.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
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

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // Limit: 20 requests max, refills 20 requests every 1 minute
        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillGreedy(20, Duration.ofMinutes(1))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ipAddress = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ipAddress, key -> createNewBucket());

        if (bucket.tryConsume(1)) {
            return true; // Allow the request to proceed to the Controller
        }

        log.warn("Rate limit triggered for IP '{}' on URI '{}'", ipAddress, request.getRequestURI());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"title\": \"Too Many Requests\", \"detail\": \"You are searching too fast. Please wait a few seconds before trying again.\"}");
        return false;
    }
}
