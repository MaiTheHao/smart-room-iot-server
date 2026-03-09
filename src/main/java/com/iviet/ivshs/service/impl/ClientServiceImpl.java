package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.ClientDao;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

  private final ClientDao clientDao;
  private final BCryptPasswordEncoder passwordEncoder;

  @Override
  public List<ClientDto> getAll() {
    return clientDao.findAll().stream()
      .map(ClientDto::from)
      .toList();
  }

  @Override
  public List<ClientDto> getAllGateways() {
    return clientDao.findAllGateways().stream()
      .map(ClientDto::from)
      .toList();
  }

  @Override
  public PaginatedResponse<ClientDto> getList(int page, int size) {
    List<ClientDto> clients = clientDao.findAll(page, size).stream()
      .map(ClientDto::from)
      .toList();
    Long totalElements = clientDao.count();
    return new PaginatedResponse<>(clients, page, size, totalElements);
  }

  @Override
  public List<Client> getAllEntities() {
    return clientDao.findAll();
  }

  @Override
  public ClientDto getById(Long clientId) {
    return ClientDto.from(clientDao.findById(clientId)
      .orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId)));
  }

  @Override
  public Client getEntityById(Long clientId) {
    Client client = clientDao.findById(clientId)
      .orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));

    if (client.getGroups() != null) {
      client.getGroups().size();
    }
    return client;
  }

  @Override
  public ClientDto getByUsername(String username) {
    return clientDao.findByUsername(username.trim())
      .map(ClientDto::from)
      .orElseThrow(() -> new NotFoundException("Client not found with username: " + username));
  }

  @Override
  public Client getEntityByUsername(String username) {
    Client client = clientDao.findByUsername(username.trim())
      .orElseThrow(() -> new NotFoundException("Client not found with username: " + username));

    if (client.getGroups() != null) {
      client.getGroups().size();
    }
    return client;
  }

  @Override
  public ClientDto getUserById(Long userId) {
    return clientDao.findUserById(userId)
      .map(ClientDto::from)
      .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
  }

  @Override
  public ClientDto getGatewayById(Long gatewayId) {
    return clientDao.findGatewayById(gatewayId)
      .map(ClientDto::from)
      .orElseThrow(() -> new NotFoundException("Gateway not found with ID: " + gatewayId));
  }

  @Override
  public ClientDto getUserByUsername(String username) {
    return clientDao.findUserByUsername(username.trim())
      .map(ClientDto::from)
      .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
  }

  @Override
  public ClientDto getUserByIpAddress(String ipAddress) {
    Client user = clientDao.findUserByIpAddress(ipAddress)
      .orElseThrow(() -> new NotFoundException("User not found with IP Address: " + ipAddress));
    return ClientDto.from(user);
  }

  @Override
  public ClientDto getGatewayByUsername(String username) {
    return clientDao.findGatewayByUsername(username.trim())
      .map(ClientDto::from)
      .orElseThrow(() -> new NotFoundException("Gateway not found with username: " + username));
  }

  @Override
  public ClientDto getGatewayByIpAddress(String ipAddress) {
    Client gateway = clientDao.findGatewayByIpAddress(ipAddress)
      .orElseThrow(() -> new NotFoundException("Gateway not found with IP Address: " + ipAddress));
    return ClientDto.from(gateway);
  }

  @Override
  public Client getFromSecurityContext() {
    Long clientId = SecurityContextUtil.getCurrentClientId();
    return getEntityById(clientId);
  }

  @Override
  public PaginatedResponse<ClientDto> getListGatewaysByRoomId(Long roomId, int page, int size) {
    List<ClientDto> gateways = clientDao.findGatewaysByRoomId(roomId, page, size).stream()
      .map(ClientDto::from)
      .toList();
    Long totalElements = clientDao.countGatewaysByRoomId(roomId);
    return new PaginatedResponse<>(gateways, page, size, totalElements);
  }

  @Override
  @Transactional
  public ClientDto create(CreateClientDto createDto) {
    String username = createDto.username() == null ? null : createDto.username().trim();
    if (username == null || username.isEmpty()) {
      throw new BadRequestException("Username is required");
    }

    if (clientDao.existsByUsername(username)) {
      throw new BadRequestException("Username already exists: " + username);
    }

    if (createDto.clientType() == ClientType.HARDWARE_GATEWAY) {
      String ipAddress = createDto.ipAddress() == null ? null : createDto.ipAddress().trim();
      if (ipAddress == null || ipAddress.isEmpty()) {
        throw new BadRequestException("IP Address is required for HARDWARE_GATEWAY clients");
      }
      if (clientDao.findGatewayByIpAddress(ipAddress).isPresent()) {
        throw new BadRequestException("IP Address already exists for another gateway: " + ipAddress);
      }
    }

    Client client = CreateClientDto.toEntity(createDto);
    client.setUsername(username);
    client.setPasswordHash(passwordEncoder.encode(createDto.password()));

    Client savedClient = clientDao.save(client);
    return ClientDto.from(savedClient);
  }

  @Override
  @Transactional
  public ClientDto update(Long clientId, UpdateClientDto updateDto) {
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
    return ClientDto.from(client);
  }

  @Override
  @Transactional
  public void delete(Long clientId) {
    Client client = clientDao.findById(clientId)
      .orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));

    if (client.getGroups() != null && !client.getGroups().isEmpty()) {
      client.getGroups().clear();
    }

    clientDao.delete(client);
  }

  @Override
  @Transactional
  public void deleteAllDeviceControl(Long clientId) {
    Client client = clientDao.findById(clientId).orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));

    if (client.getDeviceControls() != null && !client.getDeviceControls().isEmpty()) {
      client.getDeviceControls().clear();
    }

    clientDao.update(client);
  }

  @Override
  public long countGatewaysByRoomId(Long roomId) {
    return clientDao.countGatewaysByRoomId(roomId);
  }

  @Override
  public boolean existsByUsername(String username) {
    if (username == null || username.trim().isEmpty()) {
      return false;
    }
    return clientDao.existsByUsername(username.trim());
  }
}
