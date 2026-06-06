package com.iviet.ivshs.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Component
@RequiredArgsConstructor
public class HttpClientProperties {

  private final DefaultClientProperties defaultClient;

  private final GatewayControlProperties gatewayControl;

  private final GatewayTelemetryProperties gatewayTelemetry;

  private final GatewayLegacyProperties gatewayLegacy;

  @Getter
  @Component
  public static class DefaultClientProperties {
    @Value("${app.httpclient.default.default.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${app.httpclient.default.default.connection-request-timeout-ms:3000}")
    private int connectionRequestTimeoutMs;

    @Value("${app.httpclient.default.default.response-timeout-ms:5000}")
    private int responseTimeoutMs;

    @Value("${app.httpclient.default.default.max-conn-total:300}")
    private int maxConnTotal;

    @Value("${app.httpclient.default.default.max-conn-per-route:100}")
    private int maxConnPerRoute;
  }

  @Getter
  @Component
  public static class GatewayControlProperties {
    @Value("${app.httpclient.gateway.control.connect-timeout-ms:1500}")
    private int connectTimeoutMs;

    @Value("${app.httpclient.gateway.control.connection-request-timeout-ms:2000}")
    private int connectionRequestTimeoutMs;

    @Value("${app.httpclient.gateway.control.response-timeout-ms:3000}")
    private int responseTimeoutMs;

    @Value("${app.httpclient.gateway.control.max-conn-total:500}")
    private int maxConnTotal;

    @Value("${app.httpclient.gateway.control.max-conn-per-route:500}")
    private int maxConnPerRoute;
  }

  @Getter
  @Component
  public static class GatewayTelemetryProperties {
    @Value("${app.httpclient.gateway.telemetry.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${app.httpclient.gateway.telemetry.connection-request-timeout-ms:3000}")
    private int connectionRequestTimeoutMs;

    @Value("${app.httpclient.gateway.telemetry.response-timeout-ms:5000}")
    private int responseTimeoutMs;

    @Value("${app.httpclient.gateway.telemetry.max-conn-total:200}")
    private int maxConnTotal;

    @Value("${app.httpclient.gateway.telemetry.max-conn-per-route:100}")
    private int maxConnPerRoute;
  }

  @Getter
  @Component
  public static class GatewayLegacyProperties {
    @Value("${app.httpclient.gateway.legacy.connect-timeout-ms:4000}")
    private int connectTimeoutMs;

    @Value("${app.httpclient.gateway.legacy.connection-request-timeout-ms:3000}")
    private int connectionRequestTimeoutMs;

    @Value("${app.httpclient.gateway.legacy.response-timeout-ms:5000}")
    private int responseTimeoutMs;

    @Value("${app.httpclient.gateway.legacy.max-conn-total:30}")
    private int maxConnTotal;

    @Value("${app.httpclient.gateway.legacy.max-conn-per-route:15}")
    private int maxConnPerRoute;
  }
}
