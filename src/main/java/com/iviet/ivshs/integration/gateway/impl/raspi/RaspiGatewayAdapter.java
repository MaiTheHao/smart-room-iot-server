package com.iviet.ivshs.integration.gateway.impl.raspi;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.GatewayLoginResponse;
import com.iviet.ivshs.dto.LoginDto;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
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
public class RaspiGatewayAdapter implements GatewayAdapter {

    private final RaspiAuthClient authClient;
    private final RaspiSystemClient systemClient;
    private final RaspiLightControlClient lightClient;
    private final RaspiFanControlClient fanClient;
    private final RaspiAcControlClient acClient;
    private final RaspiMaintenanceClient maintenanceClient;
    private final RaspiTelemetryClient telemetryClient;

    @Override
    public ClientType getSupportedType() {
        return ClientType.HARDWARE_GATEWAY;
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
        try {
            var response = systemClient.fetchHealthCheck(ip);
            return response.getStatusCode().is2xxSuccessful()
                ? GatewayOperationResult.ok()
                : GatewayOperationResult.failure("Status: " + response.getStatusCode());
        } catch (Exception e) {
            return GatewayOperationResult.failure(e.getMessage());
        }
    }

    @Override
    public GatewayOperationResult controlDevice(String ip, GatewayCommand command) {
        try {
            ResponseEntity<ApiResponse<String>> response = dispatchControl(ip, command);
            if (response == null) {
                return GatewayOperationResult.notSupported(
                    command.category() + "/" + command.params().keySet() + " on RaspberryPi");
            }
            return response.getStatusCode().is2xxSuccessful()
                ? GatewayOperationResult.ok()
                : GatewayOperationResult.failure("Gateway error: " + response.getStatusCode());
        } catch (Exception e) {
            return GatewayOperationResult.failure(e.getMessage());
        }
    }

    private ResponseEntity<ApiResponse<String>> dispatchControl(String ip, GatewayCommand cmd) {
        if (cmd.category() == DeviceCategory.LIGHT) {
            return lightClient.controlLight(ip, cmd);
        }

        if (cmd.category() == DeviceCategory.FAN) {
            return fanClient.controlFan(ip, cmd);
        }

        if (cmd.category() == DeviceCategory.AIR_CONDITION) {
            return acClient.controlAc(ip, cmd);
        }

        return null;
    }

    @Override
    public GatewayFetchResult<EnergyMetricDto> fetchEnergyMetric(String ip, GatewayCommand command) {
        try {
            ResponseEntity<ApiResponse<EnergyMetricDto>> response =
                telemetryClient.fetchEnergyMetric(ip, command);
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getData() != null) {
                return GatewayFetchResult.ok(response.getBody().getData());
            }
            return GatewayFetchResult.failure("HTTP " + response.getStatusCode());
        } catch (Exception e) {
            return GatewayFetchResult.failure(e.getMessage());
        }
    }

    @Override
    public GatewayFetchResult<TelemetryResponseDto> fetchGlobalTelemetry(String ip) {
        try {
            ResponseEntity<TelemetryResponseDto> response = telemetryClient.fetchGlobalTelemetry(ip);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return GatewayFetchResult.ok(response.getBody());
            }
            return GatewayFetchResult.failure("HTTP " + response.getStatusCode());
        } catch (Exception e) {
            return GatewayFetchResult.failure(e.getMessage());
        }
    }

    @Override
    public GatewayOperationResult resetEnergy(String ip, GatewayCommand command) {
        try {
            ResponseEntity<ApiResponse<String>> response =
                maintenanceClient.resetEnergy(ip, command);
            return response.getStatusCode().is2xxSuccessful()
                ? GatewayOperationResult.ok()
                : GatewayOperationResult.failure("HTTP " + response.getStatusCode());
        } catch (Exception e) {
            return GatewayOperationResult.failure(e.getMessage());
        }
    }
}
