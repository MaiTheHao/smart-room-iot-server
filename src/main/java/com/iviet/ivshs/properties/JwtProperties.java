package com.iviet.ivshs.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class JwtProperties {
    
    @Value("${iviet.app.jwtSecret}")
    private String jwtSecret;
    
    @Value("${iviet.app.jwtExpirationMs}")
    private int jwtExpirationMs;
}
