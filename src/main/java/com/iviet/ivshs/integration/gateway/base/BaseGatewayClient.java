package com.iviet.ivshs.integration.gateway.base;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;
import com.iviet.ivshs.core.properties.GatewayProperties;

public abstract class BaseGatewayClient {

    @Autowired
    protected GatewayProperties gatewayProperties;

    protected String API_V1;
    protected String API_V2;

    @PostConstruct
    private void init() {
        this.API_V1 = normalizePath(gatewayProperties.getBasePath() + "/v1");
        this.API_V2 = normalizePath(gatewayProperties.getBasePath() + "/v2");
    }

    private String normalizePath(String path) {
        return path.replace("//", "/");
    }

    protected String buildUri(String ipAddress, String apiVersionPath, String endpoint) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(ipAddress.contains("://") ? ipAddress : gatewayProperties.getScheme() + "://" + ipAddress);

        if (!ipAddress.contains(":")) {
            builder.port(gatewayProperties.getPort());
        }

        return builder.path(apiVersionPath == null ? "" : apiVersionPath)
                .pathSegment(endpoint)
                .build()
                .toUriString();
    }
}
