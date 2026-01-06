package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.CreateClientDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateClientDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.enumeration.ClientTypeV1;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.ClientMapperV1;
import com.iviet.ivshs.service.ClientServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ClientServiceImplV1 implements ClientServiceV1 {

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private ClientMapperV1 clientMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public PaginatedResponse<ClientDto> getAll(int page, int size) {
        log.info("Fetching all clients, page: {}, size: {}", page, size);
        List<ClientDto> clients = clientDao.findAll(page, size).stream()
                .map(clientMapper::toDto)
                .toList();
        Long totalElements = clientDao.count();
        return new PaginatedResponse<>(clients, page, size, totalElements);
    }

    @Override
    public ClientDto getById(Long clientId) {
        log.info("Fetching client by ID: {}", clientId);
        if (clientId == null || clientId <= 0) {
            log.warn("Invalid client ID: {}", clientId);
            throw new BadRequestException("Client ID is required and must be greater than 0");
        }

        return clientMapper.toDto(clientDao.findById(clientId).orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId)));
    }

    @Override
    public Client getEntityById(Long clientId) {
        log.info("Fetching client entity by ID: {}", clientId);
        if (clientId == null || clientId <= 0) {
            log.warn("Invalid client ID: {}", clientId);
            throw new BadRequestException("Client ID is required and must be greater than 0");
        }

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
        if (username == null || username.trim().isEmpty()) {
            log.warn("Username is required");
            throw new BadRequestException("Username is required");
        }

        return clientDao.findByUsername(username.trim())
                .map(clientMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Client not found with username: " + username));
    }

    @Override
    public Client getEntityByUsername(String username) {
        log.info("Fetching client entity by username: {}", username);
        if (username == null || username.trim().isEmpty()) {
            log.warn("Username is required");
            throw new BadRequestException("Username is required");
        }
        
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
        if (userId == null || userId <= 0) {
            log.warn("Invalid user ID: {}", userId);
            throw new BadRequestException("User ID is required and must be greater than 0");
        }

        return clientDao.findUserById(userId)
                .map(clientMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    @Override
    public ClientDto getGatewayById(Long gatewayId) {
        log.info("Fetching gateway by ID: {}", gatewayId);
        if (gatewayId == null || gatewayId <= 0) {
            log.warn("Invalid gateway ID: {}", gatewayId);
            throw new BadRequestException("Gateway ID is required and must be greater than 0");
        }

        return clientDao.findGatewayById(gatewayId)
                .map(clientMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Gateway not found with ID: " + gatewayId));
    }

    @Override
    public ClientDto getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        if (username == null || username.trim().isEmpty()) {
            log.warn("Username is required");
            throw new BadRequestException("Username is required");
        }

        return clientDao.findUserByUsername(username.trim())
                .map(clientMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
    }

    @Override
    public ClientDto getUserByIpAddress(String ipAddress) {
        log.info("Fetching user by IP address: {}", ipAddress);
        if (ipAddress == null || ipAddress.isBlank()) {
            log.warn("IP Address is required");
            throw new BadRequestException("IP Address is required");
        }

        Client user = clientDao.findUserByIpAddress(ipAddress)
                .orElseThrow(() -> new NotFoundException("User not found with IP Address: " + ipAddress));

        return clientMapper.toDto(user);
    }

    @Override
    public ClientDto getGatewayByUsername(String username) {
        log.info("Fetching gateway by username: {}", username);
        if (username == null || username.trim().isEmpty()) {
            log.warn("Username is required");
            throw new BadRequestException("Username is required");
        }

        return clientDao.findGatewayByUsername(username.trim())
                .map(clientMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Gateway not found with username: " + username));
    }

    @Override
    public ClientDto getGatewayByIpAddress(String ipAddress) {
        log.info("Fetching gateway by IP address: {}", ipAddress);
        if (ipAddress == null || ipAddress.isBlank()) {
            log.warn("IP Address is required");
            throw new BadRequestException("IP Address is required");
        }

        Client gateway = clientDao.findGatewayByIpAddress(ipAddress)
                .orElseThrow(() -> new NotFoundException("Gateway not found with IP Address: " + ipAddress));

        return clientMapper.toDto(gateway);
    }

    @Override
    @Transactional
    public ClientDto create(CreateClientDto createDto) {
        log.info("Creating new client: {}", createDto);
        if (createDto == null) {
            log.warn("Client data is required");
            throw new BadRequestException("Client data is required");
        }

        String username = createDto.getUsername() == null ? null : createDto.getUsername().trim();
        if (username == null || username.isEmpty()) {
            log.warn("Username is required");
            throw new BadRequestException("Username is required");
        }

        if (clientDao.existsByUsername(username)) {
            log.warn("Username already exists: {}", username);
            throw new BadRequestException("Username already exists: " + username);
        }

        if (createDto.getClientType() == ClientTypeV1.HARDWARE_GATEWAY) {
            String ipAddress = createDto.getIpAddress() == null ? null : createDto.getIpAddress().trim();
            if (ipAddress == null || ipAddress.isEmpty()) {
                log.warn("IP Address is required for HARDWARE_GATEWAY clients");
                throw new BadRequestException("IP Address is required for HARDWARE_GATEWAY clients");
            }
            if (clientDao.findGatewayByIpAddress(ipAddress).isPresent()) {
                log.warn("IP Address already exists for another gateway: {}", ipAddress);
                throw new BadRequestException("IP Address already exists for another gateway: " + ipAddress);
            }
        } else {
            createDto.setIpAddress(null);
        }

        Client client = clientMapper.toEntity(createDto);
        client.setUsername(username);
        client.setPasswordHash(passwordEncoder.encode(createDto.getPassword()));

        Client savedClient = clientDao.save(client);

        log.info("Client created successfully: {}", savedClient.getId());
        return clientMapper.toDto(savedClient);
    }

    @Override
    @Transactional
    public ClientDto update(Long clientId, UpdateClientDto updateDto) {
        log.info("Updating client ID: {}", clientId);
        if (clientId == null) {
            log.warn("Client ID is required");
            throw new BadRequestException("Client ID is required");
        }
        if (updateDto == null) {
            log.warn("Update data is required");
            throw new BadRequestException("Update data is required");
        }

        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));

        if (updateDto.getUsername() != null && !updateDto.getUsername().trim().isEmpty()) {
            String newUsername = updateDto.getUsername().trim();
            if (!newUsername.equals(client.getUsername()) && clientDao.existsByUsername(newUsername)) {
                log.warn("Username already exists: {}", newUsername);
                throw new BadRequestException("Username already exists: " + newUsername);
            }
            client.setUsername(newUsername);
        }

        clientMapper.updateEntityFromDto(updateDto, client);

        if (updateDto.getPassword() != null && !updateDto.getPassword().trim().isEmpty()) {
            client.setPasswordHash(passwordEncoder.encode(updateDto.getPassword()));
        }

        clientDao.update(client);

        log.info("Client updated successfully: {}", clientId);
        return clientMapper.toDto(client);
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
        if (roomId == null || roomId <= 0) {
            log.warn("Room ID is required and must be greater than 0");
            throw new BadRequestException("Room ID is required and must be greater than 0");
        }

        List<ClientDto> gateways = clientDao.findGatewaysByRoomId(roomId, page, size).stream()
                .map(clientMapper::toDto)
                .toList();
        Long totalElements = clientDao.countGatewaysByRoomId(roomId);

        return new PaginatedResponse<>(gateways, page, size, totalElements);
    }

    @Override
    public Long countGateWayByRoomId(Long roomId) {
        log.info("Counting gateways by room ID: {}", roomId);
        if (roomId == null || roomId <= 0) {
            log.warn("Room ID is required and must be greater than 0");
            throw new BadRequestException("Room ID is required and must be greater than 0");
        }

        return clientDao.countGatewaysByRoomId(roomId);
    }
}