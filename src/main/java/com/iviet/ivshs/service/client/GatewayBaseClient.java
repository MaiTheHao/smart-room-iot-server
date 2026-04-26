package com.iviet.ivshs.service.client;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Base class for all gateway API clients.
 * Provides shared URL building logic and constants.
 */
public abstract class GatewayBaseClient {

    @Value("${app.gateway.port:9090}")
    protected int defaultPort;

    @Value("${app.gateway.scheme:http}")
    protected String scheme;

    @Value("${app.gateway.basePath:/smrsiot/api}")
    protected String basePath;

    protected String API_V1;
    protected String API_V2;

    @jakarta.annotation.PostConstruct
    private void init() {
        this.API_V1 = basePath + "/v1";
        this.API_V2 = basePath + "/v2";
    }

    protected void throwIfEmpty(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    protected String build(@NonNull String ip, @NonNull String basePath, @NonNull String endpoint) {
        String host = ip;
        int port = defaultPort;

        try {
            URI uri = URI.create(scheme + "://" + ip);
            if (uri.getHost() != null) {
                host = uri.getHost();
            }
            if (uri.getPort() != -1) {
                port = uri.getPort();
            }
        } catch (IllegalArgumentException e) {
            int lastColon = ip.lastIndexOf(':');
            if (lastColon != -1 && !ip.endsWith("]")) {
                host = ip.substring(0, lastColon);
                try {
                    port = Integer.parseInt(ip.substring(lastColon + 1));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String finalPath = basePath.endsWith("/") ? basePath + endpoint : basePath + "/" + endpoint;

        return UriComponentsBuilder.newInstance()
                .scheme(scheme)
                .host(host)
                .port(port)
                .path(finalPath)
                .build()
                .toUriString();
    }
}
