package com.iviet.ivshs.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class SecurityProperties {
    
    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${app.cors.allowedOrigins:*}")
    private String allowedOrigins;

    @Value("${app.cors.allowedMethods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowedHeaders:*}")
    private String allowedHeaders;
}

