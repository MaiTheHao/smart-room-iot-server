package com.iviet.ivshs.shared.filter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iviet.ivshs.core.properties.SecurityProperties;
import com.iviet.ivshs.dto.auth.CustomUserDetails;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final SecurityProperties.RateLimiter rateLimiter;
    private final Cache<String, Bucket> cache;

    public RateLimitingFilter(SecurityProperties.RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(rateLimiter.getCacheExpireMinutes(), TimeUnit.MINUTES)
                .maximumSize(rateLimiter.getCacheMaxSize())
                .build();
    }

    private Bucket createNewBucket(String path) {
        if (isHighFrequencyPath(path)) {
            return Bucket.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(rateLimiter.getHighFrequencyCapacity())
                                    .refillIntervally(1, Duration.ofMillis(rateLimiter.getHighFrequencyRefillPeriodMs()))
                                    .build())
                    .build();
        }
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(rateLimiter.getNormalCapacity())
                                .refillIntervally(1, Duration.ofMillis(rateLimiter.getNormalRefillPeriodMs()))
                                .build())
                .build();
    }

    private boolean isHighFrequencyPath(String path) {
        return path.contains("/telemetries") || path.contains("/metrics") || path.contains("/power-consumptions") || path.contains("/temperatures") || path.contains("/telemetry");
    }

    @Override
    protected void doFilterInternal(@NonNull
    HttpServletRequest request, @NonNull
    HttpServletResponse response, @NonNull
    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/")) {
            if (!rateLimiter.isEnabled()) {
                filterChain.doFilter(request, response);
                return;
            }
            String clientKey = getClientKey(request);
            String type = isHighFrequencyPath(path) ? "iot" : "gen";
            String cacheKey = clientKey + ":" + type;

            Bucket bucket = cache.get(cacheKey, k -> createNewBucket(path));

            log.debug("Rate limiting - ClientKey: {}, Path: {}, Type: {}", clientKey, path, type);
            if (bucket.tryConsume(1)) {
                log.debug("Rate limit success - ClientKey: {}, Path: {}, Type: {}, Available: {}", clientKey, path, type, bucket.getAvailableTokens());

                filterChain.doFilter(request, response);
            } else {
                log.debug("Rate limit exceeded - ClientKey: {}, Path: {}, Type: {}, Available: {}", clientKey, path, type, bucket.getAvailableTokens());

                response.setStatus(429);
                response.setHeader("Retry-After", "60");
                response.setContentType("application/json; charset=UTF-8");
                String traceId = org.slf4j.MDC.get("traceId");
                response.getWriter()
                        .write(String.format("{\"error\": \"Too many requests\", \"message\": \"System is busy, please try again later\", \"traceId\": \"%s\"}", traceId != null ? traceId : ""));
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String getClientKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return String.valueOf(userDetails.getId());
        }
        return getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
