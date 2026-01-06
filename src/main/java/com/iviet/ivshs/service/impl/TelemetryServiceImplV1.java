package com.iviet.ivshs.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDto;
import com.iviet.ivshs.dto.CreateTemperatureValueDto;
import com.iviet.ivshs.dto.FetchPowerConsumpValueResponseDto;
import com.iviet.ivshs.dto.FetchTelemetryByGatewayResponseDto;
import com.iviet.ivshs.dto.FetchTempValueResponseDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.ClientServiceV1;
import com.iviet.ivshs.service.PowerConsumptionServiceV1;
import com.iviet.ivshs.service.PowerConsumptionValueServiceV1;
import com.iviet.ivshs.service.RoomServiceV1;
import com.iviet.ivshs.service.TelemetryServiceV1;
import com.iviet.ivshs.service.TemperatureServiceV1;
import com.iviet.ivshs.service.TemperatureValueServiceV1;
import com.iviet.ivshs.util.HttpClientUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryServiceImplV1 implements TelemetryServiceV1 {

    // Temperature
    private final TemperatureServiceV1 temperatureService;
    private final TemperatureValueServiceV1 temperatureValueService;

    // Power Consumption
    private final PowerConsumptionServiceV1 powerConsumptionService;
    private final PowerConsumptionValueServiceV1 powerConsumptionValueService;

    // Client
    private final ClientServiceV1 clientService;

    // Room
    private final RoomServiceV1 roomService;

    @Override 
    public void takeByGateway(String gatewayUsername) {
        ClientDto gateway = clientService.getGatewayByUsername(gatewayUsername);
        processTakeByGateway(gateway);
    }

    @Override
    public void takeByGateway(Long gatewayId) {
        ClientDto gateway = clientService.getGatewayById(gatewayId);
        processTakeByGateway(gateway);
    }

    @Override
    public void takeByIpAddress(String gatewayIpAddress) {
        ClientDto gateway = clientService.getGatewayByIpAddress(gatewayIpAddress);
        processTakeByGateway(gateway);
    }

    @Override
    public void takeByRoom(Long roomId) {
        List<ClientDto> gateways = clientService.getAllGatewaysByRoomId(roomId, 0, 1000).content();
        if (gateways == null || gateways.size() == 0) return;

        for(ClientDto gateway : gateways) {
            processTakeByGateway(gateway);
        }
    }

    @Override
    public void takeByRoom(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) throw new BadRequestException("Room code is required");
        Long roomId = roomService.getEntityByCode(roomCode).getId();
        List<ClientDto> gateways = clientService.getAllGatewaysByRoomId(roomId, 0, 1000).content();
        if (gateways == null || gateways.size() == 0) return;

        for(ClientDto gateway : gateways) {
            processTakeByGateway(gateway);
        }
    }

    @Override
    public void takeTemperatureData(String naturalId) {
        // Stage 1: READ
        Temperature sensor = temperatureService.getEntityByNaturalId(naturalId);
        DeviceControl deviceControl = sensor.getDeviceControl();
        Client client = deviceControl.getClient();

        // Stage 2: FETCH
        String url = UrlConstant.getTelemetryTempV1(client.getIpAddress(), naturalId);
        
        var response = HttpClientUtil.get(url);
        HttpClientUtil.handleThrowException(response);

        // Stage 3: PROCESS & SAVE
        FetchTempValueResponseDto responseBody = HttpClientUtil.fromJson(response.getBody(), FetchTempValueResponseDto.class);
        CreateTemperatureValueDto createDto = CreateTemperatureValueDto.builder()
            .sensorNaturalId(naturalId)
            .tempC(responseBody.getData().getTempC())
            .timestamp(responseBody.getTimestamp())
            .build();

        temperatureValueService.createWithSensor(sensor, createDto);
    }

    @Override
    public void takePowerConsumptionData(String naturalId) {
        // Stage 1: READ
        var sensor = powerConsumptionService.getEntityByNaturalId(naturalId);
        var deviceControl = sensor.getDeviceControl();
        var client = deviceControl.getClient();

        // Stage 2: FETCH
        String url = UrlConstant.getTelemetryPowerV1(client.getIpAddress(), naturalId);
        
        var response = HttpClientUtil.get(url);
        HttpClientUtil.handleThrowException(response);

        // Stage 3: PROCESS & SAVE
        var responseBody = HttpClientUtil.fromJson(response.getBody(), FetchPowerConsumpValueResponseDto.class);
        var createDto = CreatePowerConsumptionValueDto.builder()
            .sensorNaturalId(naturalId)
            .watt(responseBody.getData().getWatt())
            .timestamp(responseBody.getTimestamp())
            .build();

        powerConsumptionValueService.createWithSensor(sensor, createDto);
    }

    private void processTakeByGateway(ClientDto gateway) {
        String url = UrlConstant.getTelemetryByGatewayV1(gateway.getIpAddress());
        var response = HttpClientUtil.get(url);
        HttpClientUtil.handleThrowException(response);

        var responseBody = HttpClientUtil.fromJson(response.getBody(), FetchTelemetryByGatewayResponseDto.class);
        Instant timestamp = responseBody.getTimestamp();
        List<FetchTelemetryByGatewayResponseDto.Data> telemetryData = responseBody.getData();

        int processedCount = 0;
        for (var data : telemetryData) {
            String naturalId = data.getNaturalId();
            try {
                switch (data.getCategory()) {
                    case TEMPERATURE -> {
                        var createDto = CreateTemperatureValueDto.builder()
                                .sensorNaturalId(naturalId)
                                .tempC(data.getData().get("tempC").asDouble())
                                .timestamp(timestamp)
                                .build();
                        temperatureValueService.createWithSensor(
                            temperatureService.getEntityByNaturalId(naturalId), createDto);
                    }
                    case POWER_CONSUMPTION -> {
                        var createDto = CreatePowerConsumptionValueDto.builder()
                                .sensorNaturalId(naturalId)
                                .watt(data.getData().get("watt").asDouble())
                                .timestamp(timestamp)
                                .build();
                        powerConsumptionValueService.createWithSensor(
                            powerConsumptionService.getEntityByNaturalId(naturalId), createDto);
                    }
                    default -> log.warn("Unknown category {} for sensor {}", data.getCategory(), naturalId);
                }
                processedCount++;
            } catch (Exception e) {
                log.error("Failed to process sensor {} for gateway {}: {}", naturalId, gateway.getUsername(), e.getMessage());
            }
        }

        log.info("Gateway {}: Processed {}/{} telemetry records", gateway.getUsername(), processedCount, telemetryData.size());
    }
}