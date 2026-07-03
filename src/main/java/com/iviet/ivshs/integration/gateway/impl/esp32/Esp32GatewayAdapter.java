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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Esp32GatewayAdapter implements GatewayAdapter {

    private final Esp32AuthClient authClient;
    private final Esp32SystemClient systemClient;
    private final Esp32LightControlClient lightClient;
    private final Esp32FanControlClient fanClient;

    @Override
    public ClientType getSupportedType() {
        return ClientType.HARDWARE_GATEWAY_ESP32;
    }

    @Override
    public ResponseEntity<ApiResponse<GatewayLoginResponse>> login(String ip, LoginDto loginDto) {
        return authClient.login(ip, loginDto);
    }

    @Override
    public ResponseEntity<SetupRequest> fetchSetup(String ip) {
        return systemClient.fetchSetup(ip);
    }

    @Override
    public GatewayOperationResult fetchHealthCheck(String ip) {
        return GatewayOperationResult.notSupported("ESP32 does not have a health-check endpoint");
    }

    @Override
    public GatewayOperationResult controlDevice(String ip, GatewayCommand command) {
        try {
            ResponseEntity<ApiResponse<String>> response = dispatchControl(ip, command);
            if (response == null) {
                return GatewayOperationResult.notSupported(
                    command.category() + " control is not supported on ESP32");
            }
            return response.getStatusCode().is2xxSuccessful()
                ? GatewayOperationResult.ok()
                : GatewayOperationResult.failure("ESP32 error: " + response.getStatusCode());
        } catch (Exception e) {
            return GatewayOperationResult.failure(e.getMessage());
        }
    }

    private ResponseEntity<ApiResponse<String>> dispatchControl(String ip, GatewayCommand command) {
        if (command.category() == DeviceCategory.LIGHT) {
            return lightClient.controlLight(ip, command);
        }

        if (command.category() == DeviceCategory.FAN) {
            return fanClient.controlFan(ip, command);
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
