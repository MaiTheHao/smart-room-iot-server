package com.iviet.ivshs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.SetupDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.ClientType;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.ExternalServiceException;
import com.iviet.ivshs.exception.domain.NetworkTimeoutException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.ClientService;
import com.iviet.ivshs.service.RoomService;
import com.iviet.ivshs.service.SetupService;
import com.iviet.ivshs.util.HttpClientUtil;
import com.iviet.ivshs.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SetupServiceImpl implements SetupService {

    @Autowired
    private ClientService clientService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SetupDao setupDao;

    @Override
    @Transactional
    public void setup(Long clientId) {
        long start = System.currentTimeMillis();
        log.info("[SETUP] Starting setup process for clientId: {}", clientId);

        Client client = validateAndGetGateway(clientId);

        SetupRequest setupRequest = fetchSetupData(client);

        executeDatabasePersistence(client, setupRequest.getData());

        log.info("[SETUP] Finished setup process for clientId: {} in {}ms", clientId, System.currentTimeMillis() - start);
    }

    private Client validateAndGetGateway(Long clientId) {
        Client client = clientService.getEntityById(clientId); 
        
        if (client.getClientType() != ClientType.HARDWARE_GATEWAY) {
            log.error("[SETUP] Client is not a gateway: id={}", clientId);
            throw new BadRequestException("Client ID " + clientId + " is not a hardware gateway");
        }
        return client;
    }

    private SetupRequest fetchSetupData(Client client) {
        String url = UrlConstant.getSetupUrlV1(client.getIpAddress());
        try {
            var res = HttpClientUtil.get(url);
            
            if (!res.isSuccess()) {
                throw new ExternalServiceException("Gateway rejected request. Status: " + res.getStatusCode());
            }

            SetupRequest req = JsonUtil.fromJson(res.getBody(), SetupRequest.class);
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

    private void validateData(SetupRequest req) {
        if (req == null) {
            throw new BadRequestException("Invalid setup data: Request is null");
        }
        if (req.getData() == null) {
            throw new BadRequestException("Invalid setup data: Data section is missing");
        }
        SetupRequest.BodyData body = req.getData();
        if (body == null || body.getRoomCode() == null || body.getRoomCode().isBlank()) {
            throw new BadRequestException("Invalid setup data: Missing room code");
        }
        if (body.getDevices() == null || body.getDevices().isEmpty()) {
            throw new BadRequestException("No devices found in setup data");
        }
    }

    protected void executeDatabasePersistence(Client client, SetupRequest.BodyData body) {
        try {
            Room room = roomService.getEntityByCode(body.getRoomCode());
            
            int processed = setupDao.persistDeviceSetup(
                body.getDevices(), 
                client.getId(), 
                room.getId()
            );

            log.info("[SETUP] Persisted {} devices for room {}", processed, body.getRoomCode());

        } catch (NotFoundException e) {
            log.error("[SETUP] Room not found in system: {}", body.getRoomCode());
            throw e;
        } catch (Exception e) {
            log.error("[SETUP] Critical error during persistence: {}", e.getMessage());
            throw new ExternalServiceException("Persistence failed: " + e.getMessage());
        }
    }
}