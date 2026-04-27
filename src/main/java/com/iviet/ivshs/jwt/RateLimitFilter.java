package com.iviet.ivshs.jwt;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "RATE-LIMIT")
public class RateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    private Bucket createNewBucket(String path) {
        if (isHighFrequencyPath(path)) {
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1))))
                    .build();
        }
        return Bucket.builder()
                .addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1))))
                .build();
    }

    private boolean isHighFrequencyPath(String path) {
        return path.contains("/telemetries") || 
               path.contains("/metrics") || 
               path.contains("/power-consumptions") || 
               path.contains("/temperatures") ||
               path.contains("/telemetry");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        if (path.startsWith("/api/")) {
            String ip = getClientIp(request);
            String type = isHighFrequencyPath(path) ? "iot" : "gen";
            String cacheKey = ip + ":" + type;

            Bucket bucket = cache.get(cacheKey, k -> createNewBucket(path));

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                // Use debug level to avoid log flooding during DDoS, 
                // but keep the info for monitoring if needed.
                log.debug("Rate limit exceeded - IP: {}, Path: {}, Type: {}", ip, path, type);
                
                response.setStatus(429);
                response.setHeader("Retry-After", "60"); // Recommended to try again after 60 seconds
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"error\": \"Too many requests\", \"message\": \"System is busy, please try again later\"}");
            }
        } else {
            filterChain.doFilter(request, response);
        }
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
