package com.iviet.ivshs.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class TokenProperties {

  @Value("${app.jwt.secret}")
  private String accessSecret;

  @Value("${app.jwt.expirationMs}")
  private long accessExpMs;
}
