package com.iviet.ivshs.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class SecurityProperties {

    @Value("${app.cors.allowedOrigins}")
    private String allowedOrigins;

    @Value("${app.cors.allowedMethods}")
    private String allowedMethods;

    @Value("${app.cors.allowedHeaders}")
    private String allowedHeaders;

    @Getter
    @Component
    public static class RateLimiter {
        @Value("${app.rate-limit.enabled}")
        private boolean enabled;

        @Value("${app.rate-limit.cache.expire-minutes}")
        private int cacheExpireMinutes;

        @Value("${app.rate-limit.cache.max-size}")
        private long cacheMaxSize;

        @Value("${app.rate-limit.high-frequency.capacity}")
        private long highFrequencyCapacity;

        @Value("${app.rate-limit.high-frequency.refill-period-ms}")
        private long highFrequencyRefillPeriodMs;

        @Value("${app.rate-limit.normal.capacity}")
        private long normalCapacity;

        @Value("${app.rate-limit.normal.refill-period-ms}")
        private long normalRefillPeriodMs;
    }
}
