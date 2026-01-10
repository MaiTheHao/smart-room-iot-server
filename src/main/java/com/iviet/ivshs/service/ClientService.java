package com.iviet.ivshs.service;

import com.iviet.ivshs.entities.Client;

import java.util.List;

import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.CreateClientDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateClientDto;

public interface ClientService {

    PaginatedResponse<ClientDto> getAll(int page, int size);

    List<ClientDto> getAll();

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

    ClientDto create(CreateClientDto createDto);

    ClientDto update(Long clientId, UpdateClientDto updateDto);

    void delete(Long clientId);

    PaginatedResponse<ClientDto> getAllGatewaysByRoomId(Long roomId, int page, int size);
    
    Long countGateWayByRoomId(Long roomId);

    boolean existsByUsername(String username);
}
