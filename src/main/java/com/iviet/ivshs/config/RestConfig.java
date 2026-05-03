package com.iviet.ivshs.config;

import com.iviet.ivshs.exception.handler.RestTemplateResponseErrorHandler;
import com.iviet.ivshs.service.client.gateway.interceptors.GatewayAuthInterceptor;
import lombok.RequiredArgsConstructor;
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
public class RestConfig {

    private final GatewayAuthInterceptor gatewayAuthInterceptor;

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(100)
                        .setMaxConnPerRoute(20)
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(Duration.ofSeconds(5));

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        return restTemplate;
    }

    /**
     * Dedicated RestTemplate for real-time device control commands (PUT/PATCH to Gateway).
     * <p>
     * Timeout policy (fail-fast):
     * <ul>
     *   <li>connectionRequestTimeout  = 1s   — max wait to acquire a slot from the pool</li>
     *   <li>connectTimeout            = 2s   — TCP handshake to Gateway</li>
     *   <li>responseTimeout           = 3s   — max wait for Gateway to return first byte</li>
     * </ul>
     *
     * @see GatewayControlClient
     */
    @Bean(name = "GatewayControlRestTemplate")
    public RestTemplate gatewayControlRestTemplate() {
        PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(60)
                .setMaxConnPerRoute(30)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(1))
                .setResponseTimeout(Timeout.ofSeconds(3))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(Duration.ofSeconds(2));

        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        restTemplate.getInterceptors().add(gatewayAuthInterceptor);
        return restTemplate;
    }

    /**
     * Dedicated RestTemplate for background telemetry/energy collection and
     * system maintenance operations (Health Check, Energy Reset).
     * <p>
     * Timeout policy (tolerant-but-bounded):
     * <ul>
     *   <li>connectionRequestTimeout  = 2s   — fail-fast queue wait</li>
     *   <li>connectTimeout            = 5s   — TCP handshake to Gateway</li>
     *   <li>responseTimeout           = 30s  — Gateway allowed up to 30s to respond</li>
     * </ul>
     *
     * @see GatewayTelemetryClient
     * @see GatewaySystemClient
     * @see GatewayMaintenanceClient
     */
    @Bean(name = "GatewayTelemetryRestTemplate")
    public RestTemplate gatewayTelemetryRestTemplate() {
        PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(25)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(2))
                .setResponseTimeout(Timeout.ofSeconds(30))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(Duration.ofSeconds(5));

        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        restTemplate.getInterceptors().add(gatewayAuthInterceptor);
        return restTemplate;
    }

    /**
     * Legacy Gateway RestTemplate used exclusively by {@link GatewayAuthClient}
     * for authentication calls triggered by {@link GatewayAuthInterceptor}.
     * <p>
     * Keeping this pool separate prevents a deadlock scenario where the interceptor
     * cannot obtain a connection to refresh a token because all pool slots are
     * held by in-flight Telemetry requests.
     */
    @Bean(name = "GatewayApiClient")
    public RestTemplate gatewayApiClient() {
        PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(20)
                .setMaxConnPerRoute(10)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(2))
                .setResponseTimeout(Timeout.ofSeconds(10))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(Duration.ofSeconds(5));

        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        restTemplate.getInterceptors().add(gatewayAuthInterceptor);
        return restTemplate;
    }
}

