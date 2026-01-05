package com.iviet.ivshs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.SetupDaoV1;
import com.iviet.ivshs.dto.SetupRequestV1;
import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.entities.RoomV1;
import com.iviet.ivshs.enumeration.ClientTypeV1;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.ExternalServiceException;
import com.iviet.ivshs.exception.domain.NetworkTimeoutException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.ClientServiceV1;
import com.iviet.ivshs.service.RoomServiceV1;
import com.iviet.ivshs.service.SetupServiceV1;
import com.iviet.ivshs.util.HttpClientUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SetupServiceImplV1 implements SetupServiceV1 {

    @Autowired
    private ClientServiceV1 clientService;

    @Autowired
    private RoomServiceV1 roomService;

    @Autowired
    private SetupDaoV1 setupDaoV1;

    @Override
    public void setup(Long clientId) {
        long start = System.currentTimeMillis();
        log.info("[SETUP] Starting setup process for clientId: {}", clientId);

        ClientV1 client = validateAndGetGateway(clientId);

        SetupRequestV1 setupRequest = fetchSetupData(client);

        executeDatabasePersistence(client, setupRequest);

        log.info("[SETUP] Finished setup process for clientId: {} in {}ms", clientId, System.currentTimeMillis() - start);
    }

    private ClientV1 validateAndGetGateway(Long clientId) {
        ClientV1 client = clientService.getEntityById(clientId); 
        
        if (client.getClientType() != ClientTypeV1.HARDWARE_GATEWAY) {
            log.error("[SETUP] Client is not a gateway: id={}", clientId);
            throw new BadRequestException("Client ID " + clientId + " is not a hardware gateway");
        }
        return client;
    }

    private SetupRequestV1 fetchSetupData(ClientV1 client) {
        String url = UrlConstant.getSetupUrlV1(client.getIpAddress());
        try {
            var res = HttpClientUtil.get(url);
            
            if (!res.isSuccess()) {
                throw new ExternalServiceException("Gateway rejected request. Status: " + res.getStatusCode());
            }

            SetupRequestV1 req = HttpClientUtil.fromJson(res.getBody(), SetupRequestV1.class);
            validateData(req);
            return req;

        } catch (NetworkTimeoutException e) {
            log.error("[SETUP] Timeout connecting to gateway: {}", client.getIpAddress());
            throw new NetworkTimeoutException("Gateway connection timed out.");
        } catch (Exception e) {
            log.error("[SETUP] Error fetching setup data: {}", e.getMessage());
            throw new ExternalServiceException("Failed to fetch setup: " + e.getMessage());
        }
    }

    private void validateData(SetupRequestV1 req) {
        if (req == null || req.getRoomCode() == null || req.getRoomCode().isBlank()) {
            throw new BadRequestException("Invalid setup data: Missing room code");
        }
        if (req.getDevices() == null || req.getDevices().isEmpty()) {
            throw new BadRequestException("No devices found in setup data");
        }
    }

    protected void executeDatabasePersistence(ClientV1 client, SetupRequestV1 req) {
        try {
            RoomV1 room = roomService.getEntityByCode(req.getRoomCode());
            
            int processed = setupDaoV1.persistDeviceSetup(
                req.getDevices(), 
                client.getId(), 
                room.getId()
            );

            log.info("[SETUP] Persisted {} devices for room {}", processed, req.getRoomCode());

        } catch (NotFoundException e) {
            log.error("[SETUP] Room not found in system: {}", req.getRoomCode());
            throw e;
        } catch (Exception e) {
            log.error("[SETUP] Critical error during persistence: {}", e.getMessage());
            throw new ExternalServiceException("Persistence failed: " + e.getMessage());
        }
    }
}