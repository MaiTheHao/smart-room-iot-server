package com.iviet.ivshs.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dto.ClientDtoV1;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.CreateTemperatureValueDtoV1;
import com.iviet.ivshs.dto.FetchPowerConsumpValueResponseDtoV1;
import com.iviet.ivshs.dto.FetchTelemetryByGatewayResponseDtoV1;
import com.iviet.ivshs.dto.FetchTempValueResponseDtoV1;
import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.entities.DeviceControlV1;
import com.iviet.ivshs.entities.TemperatureV1;
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
        ClientDtoV1 gateway = clientService.getGatewayByUsername(gatewayUsername);
        processTakeByGateway(gateway);
    }

    @Override
    public void takeByGateway(Long gatewayId) {
        ClientDtoV1 gateway = clientService.getGatewayById(gatewayId);
        processTakeByGateway(gateway);
    }

    @Override
    public void takeByIpAddress(String gatewayIpAddress) {
        ClientDtoV1 gateway = clientService.getGatewayByIpAddress(gatewayIpAddress);
        processTakeByGateway(gateway);
    }

    @Override
    public void takeByRoom(Long roomId) {
        List<ClientDtoV1> gateways = clientService.getAllGatewaysByRoomId(roomId, 0, 1000).content();
        if (gateways == null || gateways.size() == 0) return;

        for(ClientDtoV1 gateway : gateways) {
            processTakeByGateway(gateway);
        }
    }

    @Override
    public void takeByRoom(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) throw new BadRequestException("Room code is required");
        Long roomId = roomService.getEntityByCode(roomCode).getId();
        List<ClientDtoV1> gateways = clientService.getAllGatewaysByRoomId(roomId, 0, 1000).content();
        if (gateways == null || gateways.size() == 0) return;

        for(ClientDtoV1 gateway : gateways) {
            processTakeByGateway(gateway);
        }
    }

    @Override
    public void takeTemperatureData(String naturalId) {
        // Stage 1: READ
        TemperatureV1 sensor = temperatureService.getEntityByNaturalId(naturalId);
        DeviceControlV1 deviceControl = sensor.getDeviceControl();
        ClientV1 client = deviceControl.getClient();

        // Stage 2: FETCH
        String url = UrlConstant.getTelemetryTempV1(client.getIpAddress(), naturalId);
        
        var response = HttpClientUtil.get(url);
        HttpClientUtil.handleThrowException(response);

        // Stage 3: PROCESS & SAVE
        FetchTempValueResponseDtoV1 responseBody = HttpClientUtil.fromJson(response.getBody(), FetchTempValueResponseDtoV1.class);
        CreateTemperatureValueDtoV1 createDto = CreateTemperatureValueDtoV1.builder()
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
        var responseBody = HttpClientUtil.fromJson(response.getBody(), FetchPowerConsumpValueResponseDtoV1.class);
        var createDto = CreatePowerConsumptionValueDtoV1.builder()
            .sensorNaturalId(naturalId)
            .watt(responseBody.getData().getWatt())
            .timestamp(responseBody.getTimestamp())
            .build();

        powerConsumptionValueService.createWithSensor(sensor, createDto);
    }

    private void processTakeByGateway(ClientDtoV1 gateway) {
        String url = UrlConstant.getTelemetryByGatewayV1(gateway.getIpAddress());
        var response = HttpClientUtil.get(url);
        HttpClientUtil.handleThrowException(response);

        var responseBody = HttpClientUtil.fromJson(response.getBody(), FetchTelemetryByGatewayResponseDtoV1.class);
        Instant timestamp = responseBody.getTimestamp();
        List<FetchTelemetryByGatewayResponseDtoV1.Data> telemetryData = responseBody.getData();

        int processedCount = 0;
        for (var data : telemetryData) {
            String naturalId = data.getNaturalId();
            try {
                switch (data.getCategory()) {
                    case TEMPERATURE -> {
                        var createDto = CreateTemperatureValueDtoV1.builder()
                                .sensorNaturalId(naturalId)
                                .tempC(data.getData().get("tempC").asDouble())
                                .timestamp(timestamp)
                                .build();
                        temperatureValueService.createWithSensor(
                            temperatureService.getEntityByNaturalId(naturalId), createDto);
                    }
                    case POWER_CONSUMPTION -> {
                        var createDto = CreatePowerConsumptionValueDtoV1.builder()
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