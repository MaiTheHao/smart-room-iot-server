package com.iviet.ivshs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.setup.DeviceSetupOrchestrator;
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
import com.iviet.ivshs.service.client.GatewaySystemClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "SETUP")
@Service
public class SetupServiceImpl implements SetupService {

    @Autowired
    private ClientService clientService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private DeviceSetupOrchestrator deviceSetupOrchestrator;

    @Autowired
    private GatewaySystemClient gatewaySystemClient;

    @Override
    @Transactional
    public void setup(Long clientId) {
        long start = System.currentTimeMillis();
        log.info("Starting setup process for clientId: {}", clientId);

        try {
            Client client = validateAndGetGateway(clientId);
            log.info("Validated gateway: id={}, username={}, ip={}", 
                client.getId(), client.getUsername(), client.getIpAddress());

            SetupRequest setupRequest = fetchSetupData(client);

            executeDatabasePersistence(client, setupRequest.getData());

            long duration = System.currentTimeMillis() - start;
            log.info("[SETUP] Successfully completed setup for clientId: {} in {}ms", 
                clientId, duration);
                
        } catch (BadRequestException | NotFoundException e) {
            log.error("Validation error for clientId {}: {}", clientId, e.getMessage());
            throw e;
        } catch (NetworkTimeoutException | ExternalServiceException e) {
            log.error("Gateway communication error for clientId {}: {}", 
                clientId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during setup for clientId {}: {}", 
                clientId, e.getMessage(), e);
            throw new ExternalServiceException(
                "Setup process failed: " + e.getMessage()
            );
        }
    }

    private Client validateAndGetGateway(Long clientId) {
        Client client = clientService.getEntityById(clientId); 
        
        if (client.getClientType() != ClientType.HARDWARE_GATEWAY) {
            log.error("Client is not a gateway: id={}", clientId);
            throw new BadRequestException("Client ID " + clientId + " is not a hardware gateway");
        }
        
        if (client.getIpAddress() == null || client.getIpAddress().isBlank()) {
            log.error("Gateway missing IP address: id={}, username={}", 
                clientId, client.getUsername());
            throw new BadRequestException(
                "Gateway '" + client.getUsername() + "' does not have a configured IP address. " +
                "Please configure the IP address before running setup."
            );
        }
        
        return client;
    }

    private SetupRequest fetchSetupData(Client client) {
        log.info("Fetching setup data from gateway: ip={}", client.getIpAddress());
        
        try {
            SetupRequest req = gatewaySystemClient.fetchSetup(client.getIpAddress())
                .throwIfError()
                .getBody();

            validateData(req);
            
            log.info("Successfully fetched setup data: roomCode={}, devices={}", 
                req.getData().getRoomCode(), 
                req.getData().getDevices() != null ? req.getData().getDevices().size() : 0);
            
            return req;

        } catch (NetworkTimeoutException e) {
            log.error("Timeout connecting to gateway: ip={}, error={}", 
                client.getIpAddress(), e.getMessage());
            throw new NetworkTimeoutException(
                "Gateway connection timed out at " + client.getIpAddress() + ". " +
                "Please verify gateway is online and network is accessible."
            );
        } catch (ExternalServiceException e) {
            log.error("Gateway error: ip={}, error={}", client.getIpAddress(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching setup data: ip={}, error={}", 
                client.getIpAddress(), e.getMessage(), e);
            throw new ExternalServiceException(
                "Failed to communicate with gateway: " + e.getMessage()
            );
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

            int processed = deviceSetupOrchestrator.persistAll(
                body.getDevices(),
                client,
                room
            );

            log.info("Persisted {} devices for room '{}'", processed, body.getRoomCode());

        } catch (NotFoundException e) {
            log.error("Room not found in system: '{}'", body.getRoomCode());
            throw e;
        } catch (Exception e) {
            log.error("Critical error during persistence: {}", e.getMessage(), e);
            throw new ExternalServiceException("Persistence failed: " + e.getMessage());
        }
    }
}