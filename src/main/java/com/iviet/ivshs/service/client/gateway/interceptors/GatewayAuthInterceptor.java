package com.iviet.ivshs.service.client.gateway.interceptors;

import java.io.IOException;
import java.net.URI;

import com.iviet.ivshs.dto.LoginDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.service.ClientService;
import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.service.client.gateway.GatewayAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayAuthInterceptor implements ClientHttpRequestInterceptor {

    private final ClientService clientService;
    private final ClientDao clientDao;
    private final ObjectFactory<GatewayAuthClient> gatewayAuthClientFactory;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        URI uri = request.getURI();
        String ip = uri.getAuthority(); // host:port
        
        Client client = clientDao.findGatewayByIpAddress(ip).orElse(null);
        
        if (client == null) {
            log.trace("No registered gateway found for IP: {}", ip);
            return execution.execute(request, body);
        }

        // 1. Add current token if exists
        if (client.getAccessToken() != null && !client.getAccessToken().isBlank()) {
            request.getHeaders().setBearerAuth(client.getAccessToken());
        }

        ClientHttpResponse response = execution.execute(request, body);

        // 2. Handle 401 or 403
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN) {
            log.info("Gateway {} returned {}. Attempting auto-login...", ip, response.getStatusCode());
            
            if (client.getUsername() != null && client.getGatewayPassword() != null) {
                try {
                    GatewayAuthClient authClient = gatewayAuthClientFactory.getObject();
                    LoginDto loginDto = new LoginDto(client.getUsername(), client.getGatewayPassword());
                    
                    var loginResponse = authClient.login(ip, loginDto);
                    if (loginResponse.getStatusCode().is2xxSuccessful() && loginResponse.getBody() != null) {
                        String newToken = loginResponse.getBody().getData().getToken();
                        log.info("Auto-login successful for gateway {}. Retrying original request.", ip);
                        
                        // Persist new token
                        clientService.updateAccessToken(client.getId(), newToken);
                        
                        // Retry request with new token
                        HttpRequest wrappedRequest = new HttpRequestWrapper(request);
                        wrappedRequest.getHeaders().setBearerAuth(newToken);
                        return execution.execute(wrappedRequest, body);
                    } else {
                        log.error("Auto-login failed for gateway {}: {}", ip, loginResponse.getStatusCode());
                    }
                } catch (Exception e) {
                    log.error("System error during auto-login for gateway {}: {}", ip, e.getMessage());
                }
            } else {
                log.warn("Cannot perform auto-login for gateway {}: missing credentials (username={}, hasPassword={}).", 
                    ip, client.getUsername(), client.getGatewayPassword() != null);
            }
        }

        return response;
    }
}