package com.iviet.ivshs.integration.gateway;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.GatewayLoginResponse;
import com.iviet.ivshs.dto.LoginDto;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.shared.enumeration.ClientType;
import org.springframework.http.ResponseEntity;

public interface GatewayAdapter {

    ClientType getSupportedType();

    ResponseEntity<ApiResponse<GatewayLoginResponse>> login(String ip, LoginDto loginDto);

    ResponseEntity<SetupRequest> fetchSetup(String ip);

    GatewayOperationResult fetchHealthCheck(String ip);

    GatewayOperationResult controlDevice(String ip, GatewayCommand command);

    GatewayFetchResult<EnergyMetricDto> fetchEnergyMetric(String ip, GatewayCommand command);

    GatewayFetchResult<TelemetryResponseDto> fetchGlobalTelemetry(String ip);

    GatewayOperationResult resetEnergy(String ip, GatewayCommand command);
}
