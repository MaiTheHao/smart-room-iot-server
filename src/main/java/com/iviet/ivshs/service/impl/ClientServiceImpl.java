package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.SysClientFunctionCacheDao;
import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.CreateClientDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateClientDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.enumeration.ClientType;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.ClientService;
import com.iviet.ivshs.util.SecurityContextUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

    private final ClientDao clientDao;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SysClientFunctionCacheDao cacheDao;

    @Override
    public Client getFromSecurityContext() {
        Long clientId = SecurityContextUtil.getCurrentClientId();
        return getEntityById(clientId);
    }

    @Override
    public PaginatedResponse<ClientDto> getAll(int page, int size) {
        log.info("Fetching all clients, page: {}, size: {}", page, size);
        List<ClientDto> clients = clientDao.findAll(page, size).stream()
                .map(ClientDto::fromEntity)
                .toList();
        Long totalElements = clientDao.count();
        return new PaginatedResponse<>(clients, page, size, totalElements);
    }

    @Override
    public List<ClientDto> getAll() {
        log.info("Fetching all client DTOs");
        return clientDao.findAll().stream()
                .map(ClientDto::fromEntity)
                .toList();
    }

    @Override
    public List<Client> getAllEntities() {
        log.info("Fetching all client entities");
        return clientDao.findAll();
    }

    @Override
    public ClientDto getById(Long clientId) {
        log.info("Fetching client by ID: {}", clientId);
        return ClientDto.fromEntity(clientDao.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId)));
    }

    @Override
    public Client getEntityById(Long clientId) {
        log.info("Fetching client entity by ID: {}", clientId);
        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));
        
        if (client.getGroups() != null) {
            client.getGroups().size();
        }
        return client;
    }

    @Override
    public ClientDto getByUsername(String username) {
        log.info("Fetching client by username: {}", username);
        return clientDao.findByUsername(username.trim())
                .map(ClientDto::fromEntity)
                .orElseThrow(() -> new NotFoundException("Client not found with username: " + username));
    }

    @Override
    public Client getEntityByUsername(String username) {
        log.info("Fetching client entity by username: {}", username);
        Client client = clientDao.findByUsername(username.trim())
                .orElseThrow(() -> new NotFoundException("Client not found with username: " + username));

        if (client.getGroups() != null) {
            client.getGroups().size(); 
        }
        return client;
    }

    @Override
    public ClientDto getUserById(Long userId) {
        log.info("Fetching user by ID: {}", userId);
        return clientDao.findUserById(userId)
                .map(ClientDto::fromEntity)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    @Override
    public ClientDto getGatewayById(Long gatewayId) {
        log.info("Fetching gateway by ID: {}", gatewayId);
        return clientDao.findGatewayById(gatewayId)
                .map(ClientDto::fromEntity)
                .orElseThrow(() -> new NotFoundException("Gateway not found with ID: " + gatewayId));
    }

    @Override
    public ClientDto getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        return clientDao.findUserByUsername(username.trim())
                .map(ClientDto::fromEntity)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
    }

    @Override
    public ClientDto getUserByIpAddress(String ipAddress) {
        log.info("Fetching user by IP address: {}", ipAddress);
        Client user = clientDao.findUserByIpAddress(ipAddress)
                .orElseThrow(() -> new NotFoundException("User not found with IP Address: " + ipAddress));
        return ClientDto.fromEntity(user);
    }

    @Override
    public ClientDto getGatewayByUsername(String username) {
        log.info("Fetching gateway by username: {}", username);
        return clientDao.findGatewayByUsername(username.trim())
                .map(ClientDto::fromEntity)
                .orElseThrow(() -> new NotFoundException("Gateway not found with username: " + username));
    }

    @Override
    public ClientDto getGatewayByIpAddress(String ipAddress) {
        log.info("Fetching gateway by IP address: {}", ipAddress);
        Client gateway = clientDao.findGatewayByIpAddress(ipAddress)
                .orElseThrow(() -> new NotFoundException("Gateway not found with IP Address: " + ipAddress));
        return ClientDto.fromEntity(gateway);
    }

    @Override
    @Transactional
    public ClientDto create(CreateClientDto createDto) {
        log.info("Creating new client with username: {}", createDto.username());

        String username = createDto.username() == null ? null : createDto.username().trim();
        if (username == null || username.isEmpty()) {
            log.warn("Username is required");
            throw new BadRequestException("Username is required");
        }

        if (clientDao.existsByUsername(username)) {
            log.warn("Username already exists: {}", username);
            throw new BadRequestException("Username already exists: " + username);
        }

        if (createDto.clientType() == ClientType.HARDWARE_GATEWAY) {
            String ipAddress = createDto.ipAddress() == null ? null : createDto.ipAddress().trim();
            if (ipAddress == null || ipAddress.isEmpty()) {
                log.warn("IP Address is required for HARDWARE_GATEWAY clients");
                throw new BadRequestException("IP Address is required for HARDWARE_GATEWAY clients");
            }
            if (clientDao.findGatewayByIpAddress(ipAddress).isPresent()) {
                log.warn("IP Address already exists for another gateway: {}", ipAddress);
                throw new BadRequestException("IP Address already exists for another gateway: " + ipAddress);
            }
        }

        Client client = CreateClientDto.toEntity(createDto);
        client.setUsername(username);
        client.setPasswordHash(passwordEncoder.encode(createDto.password()));

        Client savedClient = clientDao.save(client);

        log.info("Client created successfully with ID: {}", savedClient.getId());
        return ClientDto.fromEntity(savedClient);
    }

    @Override
    @Transactional
    public ClientDto update(Long clientId, UpdateClientDto updateDto) {
        log.info("Updating client ID: {}", clientId);

        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));

        if (updateDto.clientType() != null) {
            client.setClientType(updateDto.clientType());
        }
        if (updateDto.ipAddress() != null) {
            client.setIpAddress(updateDto.ipAddress());
        }
        if (updateDto.macAddress() != null) {
            client.setMacAddress(updateDto.macAddress());
        }
        if (updateDto.avatarUrl() != null) {
            client.setAvatarUrl(updateDto.avatarUrl());
        }

        if (updateDto.password() != null && !updateDto.password().trim().isEmpty()) {
            client.setPasswordHash(passwordEncoder.encode(updateDto.password()));
        }

        clientDao.update(client);

        log.info("Client updated successfully: {}", clientId);
        return ClientDto.fromEntity(client);
    }

    @Override
    @Transactional
    public void delete(Long clientId) {
        log.info("Deleting client ID: {}", clientId);
        if (clientId == null) {
            log.warn("Client ID is required");
            throw new BadRequestException("Client ID is required");
        }

        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));

        log.info("Deleting function cache for client ID: {}", clientId);
        int deletedCacheRecords = cacheDao.deleteByClient(clientId);
        log.info("Deleted {} cache records for client ID: {}", deletedCacheRecords, clientId);
        
        if (client.getGroups() != null && !client.getGroups().isEmpty()) {
            log.info("Client has {} group(s). Clearing group associations...", client.getGroups().size());
            client.getGroups().clear();
        }

        clientDao.delete(client);
        log.info("Client deleted successfully: {}", clientId);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Username is required for existsByUsername");
            return false;
        }
        return clientDao.existsByUsername(username.trim());
    }

    @Override
    public PaginatedResponse<ClientDto> getAllGatewaysByRoomId(Long roomId, int page, int size) {
        log.info("Fetching all gateways by room ID: {}, page: {}, size: {}", roomId, page, size);

        List<ClientDto> gateways = clientDao.findGatewaysByRoomId(roomId, page, size).stream()
                .map(ClientDto::fromEntity)
                .toList();
        Long totalElements = clientDao.countGatewaysByRoomId(roomId);

        return new PaginatedResponse<>(gateways, page, size, totalElements);
    }

    @Override
    public Long countGateWayByRoomId(Long roomId) {
        log.info("Counting gateways by room ID: {}", roomId);
        return clientDao.countGatewaysByRoomId(roomId);
    }
}
