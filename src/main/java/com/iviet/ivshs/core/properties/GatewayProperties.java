package com.iviet.ivshs.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class GatewayProperties {

  @Value("${app.gateway.port}")
  private int port;

  @Value("${app.gateway.scheme}")
  private String scheme;

  @Value("${app.gateway.base-path}")
  private String basePath;
}
