package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.CreateClientDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateClientDto;
import com.iviet.ivshs.entities.Client;

import java.util.List;

public interface ClientService {

  List<ClientDto> getAll();

  List<ClientDto> getAllGateways();

  PaginatedResponse<ClientDto> getList(int page, int size);

  List<Client> getAllEntities();

  Client getFromSecurityContext();

  ClientDto getById(Long clientId);

  Client getEntityById(Long clientId);

  ClientDto getByUsername(String username);

  Client getEntityByUsername(String username);

  ClientDto getUserById(Long userId);

  ClientDto getGatewayById(Long gatewayId);

  ClientDto getUserByUsername(String username);

  ClientDto getUserByIpAddress(String ipAddress);

  ClientDto getGatewayByUsername(String username);

  ClientDto getGatewayByIpAddress(String ipAddress);

  Client getGatewayEntityByIpAddress(String ipAddress);

  void updateAccessToken(Long clientId, String accessToken);

  PaginatedResponse<ClientDto> getListGatewaysByRoomId(Long roomId, int page, int size);

  ClientDto create(CreateClientDto createDto);

  ClientDto update(Long clientId, UpdateClientDto updateDto);

  void delete(Long clientId);

  void deleteAllDeviceControl(Long clientId);

  long countGatewaysByRoomId(Long roomId);

  boolean existsByUsername(String username);
}
