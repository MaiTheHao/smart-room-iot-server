package com.iviet.ivshs.integration.gateway.impl.esp32;

import com.iviet.ivshs.dto.auth.GatewayLoginResponse;
import com.iviet.ivshs.dto.auth.LoginDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.metric.EnergyMetricDto;
import com.iviet.ivshs.dto.metric.TelemetryResponseDto;
import com.iviet.ivshs.dto.setup.SetupRequest;
import com.iviet.ivshs.integration.gateway.*;
import com.iviet.ivshs.shared.enumeration.ClientType;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class Esp32GatewayAdapter extends Esp32BaseClient implements GatewayAdapter {

    private final RestTemplate controlRestTemplate;
    private final RestTemplate telemetryRestTemplate;
    private final RestTemplate authRestTemplate;

    public Esp32GatewayAdapter(
            @Qualifier("GatewayControlRestTemplate") RestTemplate controlRestTemplate,
            @Qualifier("GatewayTelemetryRestTemplate") RestTemplate telemetryRestTemplate,
            @Qualifier("GatewayApiClient") RestTemplate authRestTemplate) {
        this.controlRestTemplate = controlRestTemplate;
        this.telemetryRestTemplate = telemetryRestTemplate;
        this.authRestTemplate = authRestTemplate;
    }

    @Override
    public ClientType getSupportedType() {
        return ClientType.HARDWARE_GATEWAY_ESP32;
    }

    @Override
    public ResponseEntity<ApiResponse<GatewayLoginResponse>> login(String ip, LoginDto loginDto) {
        String url = buildEsp32Uri(ip, "auth/login");
        HttpEntity<LoginDto> request = new HttpEntity<>(loginDto);
        return authRestTemplate.exchange(url, HttpMethod.POST, request,
            new ParameterizedTypeReference<ApiResponse<GatewayLoginResponse>>() {});
    }

    @Override
    public ResponseEntity<SetupRequest> fetchSetup(String ip) {
        String url = buildEsp32Uri(ip, "setup");
        return telemetryRestTemplate.exchange(url, HttpMethod.GET, null,
            new ParameterizedTypeReference<SetupRequest>() {});
    }

    @Override
    public GatewayOperationResult fetchHealthCheck(String ip) {
        return GatewayOperationResult.notSupported("ESP32 does not have a health-check endpoint");
    }

    @Override
    public GatewayOperationResult controlDevice(String ip, GatewayCommand command) {
        Map<String, Object> body = buildControlBody(command);
        if (body == null) {
            return GatewayOperationResult.notSupported(
                command.category() + " control is not supported on ESP32");
        }
        try {
            String url = buildEsp32Uri(ip, "control");
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);
            ResponseEntity<ApiResponse<String>> response = controlRestTemplate.exchange(
                url, HttpMethod.POST, request,
                new ParameterizedTypeReference<ApiResponse<String>>() {});
            return response.getStatusCode().is2xxSuccessful()
                ? GatewayOperationResult.ok()
                : GatewayOperationResult.failure("ESP32 error: " + response.getStatusCode());
        } catch (Exception e) {
            return GatewayOperationResult.failure(e.getMessage());
        }
    }

    private Map<String, Object> buildControlBody(GatewayCommand command) {
        if (command.category() == DeviceCategory.LIGHT) {
            Object power = command.param("power");
            if (power == null) return null;
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("naturalId", command.naturalId());
            body.put("category", "LIGHTING");
            body.put("power", Boolean.TRUE.equals(power) ? "ON" : "OFF");
            return body;
        }

        if (command.category() == DeviceCategory.FAN) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("naturalId", command.naturalId());
            body.put("category", "FAN");
            Object power = command.param("power");
            Object speed = command.param("speed");
            if (power != null) body.put("power", Boolean.TRUE.equals(power) ? "ON" : "OFF");
            if (speed != null) body.put("speed", speed);
            if (body.size() == 2) return null;
            return body;
        }

        return null;
    }

    @Override
    public GatewayFetchResult<EnergyMetricDto> fetchEnergyMetric(String ip, GatewayCommand command) {
        return GatewayFetchResult.notSupported("ESP32 does not expose per-device energy metric API");
    }

    @Override
    public GatewayFetchResult<TelemetryResponseDto> fetchGlobalTelemetry(String ip) {
        return GatewayFetchResult.notSupported("ESP32 does not expose global telemetry API");
    }

    @Override
    public GatewayOperationResult resetEnergy(String ip, GatewayCommand command) {
        return GatewayOperationResult.notSupported("ESP32 does not support energy reset");
    }
}
