package com.iviet.ivshs.core.config;

import com.iviet.ivshs.core.properties.HttpClientProperties;
import com.iviet.ivshs.integration.gateway.interceptor.GatewayAuthInterceptor;
import com.iviet.ivshs.integration.gateway.interceptor.TraceForwardingInterceptor;
import com.iviet.ivshs.shared.exception.handler.RestTemplateResponseErrorHandler;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final GatewayAuthInterceptor gatewayAuthInterceptor;
    private final TraceForwardingInterceptor traceForwardingInterceptor;
    private final HttpClientProperties httpClientProperties;

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        HttpClientProperties.DefaultClientProperties props = httpClientProperties.getDefaultClient();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofMillis(props.getConnectTimeoutMs())))
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(Duration.ofMillis(props.getConnectionRequestTimeoutMs())))
                .setResponseTimeout(Timeout.of(Duration.ofMillis(props.getResponseTimeoutMs())))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(props.getMaxConnTotal())
                        .setMaxConnPerRoute(props.getMaxConnPerRoute())
                        .setDefaultConnectionConfig(connectionConfig)
                        .build())
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        restTemplate.getInterceptors().add(traceForwardingInterceptor);
        return restTemplate;
    }

    /**
     * Dedicated RestTemplate for real-time device control commands (PUT/PATCH to
     * Gateway).
     */
    @Bean(name = "GatewayControlRestTemplate")
    public RestTemplate gatewayControlRestTemplate() {
        HttpClientProperties.GatewayControlProperties props = httpClientProperties.getGatewayControl();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofMillis(props.getConnectTimeoutMs())))
                .build();

        PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(props.getMaxConnTotal())
                .setMaxConnPerRoute(props.getMaxConnPerRoute())
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(Duration.ofMillis(props.getConnectionRequestTimeoutMs())))
                .setResponseTimeout(Timeout.of(Duration.ofMillis(props.getResponseTimeoutMs())))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        restTemplate.getInterceptors().add(traceForwardingInterceptor);
        restTemplate.getInterceptors().add(gatewayAuthInterceptor);
        return restTemplate;
    }

    /**
     * Dedicated RestTemplate for background telemetry/energy collection and
     * system maintenance operations (Health Check, Energy Reset).
     */
    @Bean(name = "GatewayTelemetryRestTemplate")
    public RestTemplate gatewayTelemetryRestTemplate() {
        HttpClientProperties.GatewayTelemetryProperties props = httpClientProperties.getGatewayTelemetry();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofMillis(props.getConnectTimeoutMs())))
                .build();

        PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(props.getMaxConnTotal())
                .setMaxConnPerRoute(props.getMaxConnPerRoute())
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(Duration.ofMillis(props.getConnectionRequestTimeoutMs())))
                .setResponseTimeout(Timeout.of(Duration.ofMillis(props.getResponseTimeoutMs())))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        restTemplate.getInterceptors().add(traceForwardingInterceptor);
        restTemplate.getInterceptors().add(gatewayAuthInterceptor);
        return restTemplate;
    }

    /**
     * Legacy Gateway RestTemplate used exclusively by {@link GatewayAuthClient}
     * for authentication calls triggered by {@link GatewayAuthInterceptor}.
     */
    @Bean(name = "GatewayApiClient")
    public RestTemplate gatewayApiClient() {
        HttpClientProperties.GatewayLegacyProperties props = httpClientProperties.getGatewayLegacy();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofMillis(props.getConnectTimeoutMs())))
                .build();

        PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(props.getMaxConnTotal())
                .setMaxConnPerRoute(props.getMaxConnPerRoute())
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(Duration.ofMillis(props.getConnectionRequestTimeoutMs())))
                .setResponseTimeout(Timeout.of(Duration.ofMillis(props.getResponseTimeoutMs())))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        restTemplate.getInterceptors().add(traceForwardingInterceptor);
        restTemplate.getInterceptors().add(gatewayAuthInterceptor);
        return restTemplate;
    }
}
