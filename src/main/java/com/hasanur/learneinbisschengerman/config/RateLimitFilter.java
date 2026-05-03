package com.hasanur.learneinbisschengerman.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-limiting filter that protects auth endpoints ({@code /auth/**})
 * against brute-force attacks using the token-bucket algorithm via Bucket4j.
 *
 * <p>Each client IP gets its own bucket: <strong>10 requests per minute</strong>.
 * When the limit is exceeded, the filter short-circuits the request with
 * HTTP 429 (Too Many Requests) and a {@code Retry-After} header.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    /** Max requests allowed per time window. */
    private static final int CAPACITY = 10;

    /** Time window for the rate limit. */
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /** Upper bound on tracked IPs to prevent memory exhaustion from spoofed addresses. */
    private static final int MAX_BUCKETS = 10_000;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000 + 1;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(waitSeconds));
            response.getWriter().write(
                    "{\"error\":\"Too many requests\",\"retryAfterSeconds\":" + waitSeconds + "}");
        }
    }

    /**
     * Only apply this filter to auth routes.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/auth/");
    }

    private Bucket createBucket() {
        // Evict the map if it grows too large (simple protection against IP spoofing DoS)
        if (buckets.size() >= MAX_BUCKETS) {
            buckets.clear();
        }

        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(CAPACITY)
                        .refillGreedy(CAPACITY, WINDOW)
                        .build())
                .build();
    }

    /**
     * Resolves the real client IP, respecting the {@code X-Forwarded-For} header
     * when the app sits behind a reverse proxy.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // First IP in the chain is the original client
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
