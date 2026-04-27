package com.iviet.ivshs.service.client.gateway;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class GatewayBaseClient {

    @Value("${app.gateway.port:9090}")
    protected int defaultPort;

    @Value("${app.gateway.scheme:http}")
    protected String scheme;

    @Value("${app.gateway.base-path:/api}")
    protected String basePath;

    protected String API_V1;
    protected String API_V2;

    @PostConstruct
    private void init() {
        this.API_V1 = normalizePath(basePath + "/v1");
        this.API_V2 = normalizePath(basePath + "/v2");
    }

    private String normalizePath(String path) {
        return path.replace("//", "/");
    }

    protected String buildUri(String ipAddress, String apiVersionPath, String endpoint) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                ipAddress.contains("://") ? ipAddress : scheme + "://" + ipAddress);
        
        if (!ipAddress.contains(":")) {
            builder.port(defaultPort);
        }
        
        return builder.path(apiVersionPath == null ? "" : apiVersionPath)
                .pathSegment(endpoint)
                .build()
                .toUriString();
    }
}