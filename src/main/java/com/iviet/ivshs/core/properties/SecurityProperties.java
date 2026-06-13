package com.iviet.ivshs.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class SecurityProperties {

    @Value("${app.cors.allowedOrigins:*}")
    private String allowedOrigins;

    @Value("${app.cors.allowedMethods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowedHeaders:*}")
    private String allowedHeaders;

    @Getter
    @Component
    public static class RateLimiter {
        @Value("${app.rate-limit.enabled:true}")
        private boolean enabled;

        @Value("${app.rate-limit.cache.expire-minutes:10}")
        private int cacheExpireMinutes;

        @Value("${app.rate-limit.cache.max-size:10000}")
        private long cacheMaxSize;

        @Value("${app.rate-limit.high-frequency.capacity:100}")
        private long highFrequencyCapacity;

        @Value("${app.rate-limit.high-frequency.refill-period-ms:1200}")
        private long highFrequencyRefillPeriodMs;

        @Value("${app.rate-limit.normal.capacity:20}")
        private long normalCapacity;

        @Value("${app.rate-limit.normal.refill-period-ms:3000}")
        private long normalRefillPeriodMs;
    }
}
