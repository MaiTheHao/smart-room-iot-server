package com.iviet.ivshs.integration.gateway.impl.raspi;

import com.iviet.ivshs.dto.auth.GatewayLoginResponse;
import com.iviet.ivshs.dto.auth.LoginDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.AcRemoteRequestPayload;
import com.iviet.ivshs.dto.control.DeviceControlPayload;
import com.iviet.ivshs.dto.metric.EnergyMetricDto;
import com.iviet.ivshs.dto.metric.TelemetryResponseDto;
import com.iviet.ivshs.dto.setup.SetupRequest;
import com.iviet.ivshs.integration.gateway.*;
import com.iviet.ivshs.shared.enumeration.ClientType;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;
import com.iviet.ivshs.shared.exception.BadRequestException;
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
        String naturalId = cmd.naturalId();
        DeviceSpecificType specificType = cmd.specificType();
        Integer duration = cmd.duration();

        if (cmd.category() == DeviceCategory.LIGHT) {
            Object power = cmd.param("power");
            Object level = cmd.param("level");
            if (power != null) {
                return lightClient.controlLightPower(ip, naturalId,
                    DeviceControlPayload.of(specificType, power));
            }
            if (level != null) {
                return lightClient.controlLightLevel(ip, naturalId,
                    DeviceControlPayload.of(specificType, level));
            }
        }

        if (cmd.category() == DeviceCategory.FAN) {
            Object power = cmd.param("power");
            Object speed = cmd.param("speed");
            Object mode = cmd.param("mode");
            Object swing = cmd.param("swing");
            if (power != null) return fanClient.controlFanPower(ip, naturalId,
                DeviceControlPayload.of(specificType, duration, power));
            if (speed != null) return fanClient.controlFanSpeed(ip, naturalId,
                DeviceControlPayload.of(specificType, duration, speed));
            if (mode != null) return fanClient.controlFanMode(ip, naturalId,
                DeviceControlPayload.of(specificType, duration, mode));
            if (swing != null) return fanClient.controlFanSwing(ip, naturalId,
                DeviceControlPayload.of(specificType, duration, swing));
        }

        if (cmd.category() == DeviceCategory.AIR_CONDITION) {
            AcRemoteRequestPayload acPayload = buildAcPayload(cmd);
            Object power = cmd.param("power");
            if (power != null) return acClient.controlAcPower(ip, naturalId, acPayload);
            return acClient.controlAcRemote(ip, naturalId, acPayload);
        }

        return null;
    }

    private AcRemoteRequestPayload buildAcPayload(GatewayCommand cmd) {
        return AcRemoteRequestPayload.builder()
            .power(stringParam(cmd, "power"))
            .temperature(intParam(cmd, "temperature"))
            .mode(stringParam(cmd, "mode"))
            .speed(intParam(cmd, "speed"))
            .swing(stringParam(cmd, "swing"))
            .duration(cmd.duration())
            .specificType(cmd.specificType())
            .build();
    }

    @Override
    public GatewayFetchResult<EnergyMetricDto> fetchEnergyMetric(String ip, GatewayCommand command) {
        try {
            String pathSegment = resolveGatewayPath(command);
            ResponseEntity<ApiResponse<EnergyMetricDto>> response =
                telemetryClient.fetchByPath(ip, pathSegment, command.naturalId());
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
            String pathSegment = resolveGatewayPath(command);
            ResponseEntity<ApiResponse<String>> response =
                maintenanceClient.resetByPath(ip, pathSegment, command.naturalId());
            return response.getStatusCode().is2xxSuccessful()
                ? GatewayOperationResult.ok()
                : GatewayOperationResult.failure("HTTP " + response.getStatusCode());
        } catch (Exception e) {
            return GatewayOperationResult.failure(e.getMessage());
        }
    }

    private String resolveGatewayPath(GatewayCommand command) {
        String hint = command.metaGatewayPath();
        if (hint != null) return hint;
        return switch (command.category()) {
            case LIGHT -> "light";
            case FAN -> "fan";
            case AIR_CONDITION -> "ac";
            case POWER_CONSUMPTION -> "power-consumption";
            default -> throw new BadRequestException(
                "No gateway path mapping for DeviceCategory: " + command.category());
        };
    }

    private String stringParam(GatewayCommand cmd, String key) {
        Object val = cmd.param(key);
        return val != null ? val.toString() : null;
    }

    private Integer intParam(GatewayCommand cmd, String key) {
        Object val = cmd.param(key);
        return val instanceof Number n ? n.intValue() : null;
    }
}
