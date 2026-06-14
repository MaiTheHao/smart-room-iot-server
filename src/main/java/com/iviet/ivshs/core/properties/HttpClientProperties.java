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
    @Value("${app.httpclient.default.default.connect-timeout-ms}")
    private int connectTimeoutMs;

    @Value("${app.httpclient.default.default.connection-request-timeout-ms}")
    private int connectionRequestTimeoutMs;

    @Value("${app.httpclient.default.default.response-timeout-ms}")
    private int responseTimeoutMs;

    @Value("${app.httpclient.default.default.max-conn-total}")
    private int maxConnTotal;

    @Value("${app.httpclient.default.default.max-conn-per-route}")
    private int maxConnPerRoute;
  }

  @Getter
  @Component
  public static class GatewayControlProperties {
    @Value("${app.httpclient.gateway.control.connect-timeout-ms}")
    private int connectTimeoutMs;

    @Value("${app.httpclient.gateway.control.connection-request-timeout-ms}")
    private int connectionRequestTimeoutMs;

    @Value("${app.httpclient.gateway.control.response-timeout-ms}")
    private int responseTimeoutMs;

    @Value("${app.httpclient.gateway.control.max-conn-total}")
    private int maxConnTotal;

    @Value("${app.httpclient.gateway.control.max-conn-per-route}")
    private int maxConnPerRoute;
  }

  @Getter
  @Component
  public static class GatewayTelemetryProperties {
    @Value("${app.httpclient.gateway.telemetry.connect-timeout-ms}")
    private int connectTimeoutMs;

    @Value("${app.httpclient.gateway.telemetry.connection-request-timeout-ms}")
    private int connectionRequestTimeoutMs;

    @Value("${app.httpclient.gateway.telemetry.response-timeout-ms}")
    private int responseTimeoutMs;

    @Value("${app.httpclient.gateway.telemetry.max-conn-total}")
    private int maxConnTotal;

    @Value("${app.httpclient.gateway.telemetry.max-conn-per-route}")
    private int maxConnPerRoute;
  }

  @Getter
  @Component
  public static class GatewayLegacyProperties {
    @Value("${app.httpclient.gateway.legacy.connect-timeout-ms}")
    private int connectTimeoutMs;

    @Value("${app.httpclient.gateway.legacy.connection-request-timeout-ms}")
    private int connectionRequestTimeoutMs;

    @Value("${app.httpclient.gateway.legacy.response-timeout-ms}")
    private int responseTimeoutMs;

    @Value("${app.httpclient.gateway.legacy.max-conn-total}")
    private int maxConnTotal;

    @Value("${app.httpclient.gateway.legacy.max-conn-per-route}")
    private int maxConnPerRoute;
  }
}
