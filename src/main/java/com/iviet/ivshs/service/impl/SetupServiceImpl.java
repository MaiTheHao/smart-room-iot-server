package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.setup.DeviceSetupOrchestrator;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.ClientType;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.ExternalServiceException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.ClientService;
import com.iviet.ivshs.service.RoomService;
import com.iviet.ivshs.service.SetupService;
import com.iviet.ivshs.service.client.gateway.GatewaySystemClient;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SetupServiceImpl implements SetupService {

    private final ClientService clientService;
    private final RoomService roomService;
    private final DeviceSetupOrchestrator deviceSetupOrchestrator;
    private final GatewaySystemClient gatewaySystemClient;

    @Override
    @Transactional
    public void setup(Long clientId) {
        try {
            Client client = validateAndGetGateway(clientId);
            SetupRequest setupRequest = fetchSetupData(client);
            executeDatabasePersistence(client, setupRequest.getData());
        } catch (BadRequestException | NotFoundException | ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("Setup process failed: " + e.getMessage());
        }
    }

    private Client validateAndGetGateway(Long clientId) {
        Client client = clientService.getEntityById(clientId); 
        
        if (client.getClientType() != ClientType.HARDWARE_GATEWAY) {
            throw new BadRequestException("Client ID " + clientId + " is not a hardware gateway");
        }
        
        if (client.getIpAddress() == null || client.getIpAddress().isBlank()) {
            throw new BadRequestException(
                "Gateway '" + client.getUsername() + "' does not have a configured IP address. " +
                "Please configure the IP address before running setup."
            );
        }
        
        return client;
    }

    private SetupRequest fetchSetupData(Client client) {
        try {
            ResponseEntity<SetupRequest> response = gatewaySystemClient.fetchSetup(client.getIpAddress());
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExternalServiceException("Gateway returned error status: " + response.getStatusCode());
            }

            SetupRequest req = response.getBody();
            if (req.getData() == null) {
                throw new ExternalServiceException("Gateway returned empty setup data");
            }

            validateData(req);
            return req;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("Failed to communicate with gateway: " + e.getMessage());
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

            deviceSetupOrchestrator.persistAll(
                body.getDevices(),
                client,
                room
            );
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("Persistence failed: " + e.getMessage());
        }
    }
}