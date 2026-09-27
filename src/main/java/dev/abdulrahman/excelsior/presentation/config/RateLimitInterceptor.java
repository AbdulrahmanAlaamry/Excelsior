package dev.abdulrahman.excelsior.presentation.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // A simple in-memory cache to hold a bucket for each IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // Limit: 5 requests max, refills 5 requests every 1 minute
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get the user's IP address
        String ipAddress = request.getRemoteAddr();
        
        // Fetch their bucket, or create a new one if they are a new visitor
        Bucket bucket = buckets.computeIfAbsent(ipAddress, key -> createNewBucket());

        // Try to consume 1 token
        if (bucket.tryConsume(1)) {
            return true; // Allow the request to proceed to the Controller
        }

        // If no tokens are left, reject the request with HTTP 429
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"title\": \"Too Many Requests\", \"detail\": \"Please wait a minute before searching again.\"}");
        return false;
    }
}
