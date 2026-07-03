package com.iviet.ivshs.integration.gateway.impl.esp32;

import com.iviet.ivshs.integration.gateway.base.BaseGatewayClient;

public abstract class Esp32BaseClient extends BaseGatewayClient {
    protected String buildEsp32Uri(String ip, String endpoint) {
        return buildUri(ip, null, endpoint);
    }
}
