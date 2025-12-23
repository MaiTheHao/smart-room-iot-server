package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.ClientDtoV1;
import com.iviet.ivshs.dto.CreateClientDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.UpdateClientDtoV1;

public interface ClientServiceV1 {
    PaginatedResponseV1<ClientDtoV1> getAllClients(int page, int size);
    ClientDtoV1 getClientById(Long clientId);
    ClientDtoV1 createClient(CreateClientDtoV1 createDto);
    ClientDtoV1 updateClient(Long clientId, UpdateClientDtoV1 updateDto);
    void deleteClient(Long clientId);
    boolean existsByUsername(String username);
    PaginatedResponseV1<ClientDtoV1> getClientsByRoomId(Long roomId, int page, int size);
    Long countGateWayByRoomId(Long roomId);
}

