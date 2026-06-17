package com.iviet.ivshs.shared.exception.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestTemplateResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public boolean hasError(@NonNull
    ClientHttpResponse response) throws IOException {
        if (super.hasError(response)) {
            String body = "";
            try {
                body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Failed to read response body: {}", e.getMessage());
            }
            log.error("RestTemplate error | Status: {} {} | Response: {}", response.getStatusCode(), response.getStatusText(), body);
        }
        return false;
    }

    @Override
    public void handleError(@NonNull
    ClientHttpResponse response) throws IOException {}
}
