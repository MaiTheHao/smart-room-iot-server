package com.iviet.ivshs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.ClientDaoV1;
import com.iviet.ivshs.dto.ClientDtoV1;
import com.iviet.ivshs.dto.CreateClientDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.UpdateClientDtoV1;
import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.exception.BadRequestException;
import com.iviet.ivshs.exception.NotFoundException;
import com.iviet.ivshs.mapper.ClientMapperV1;
import com.iviet.ivshs.service.ClientServiceV1;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientServiceImplV1 implements ClientServiceV1 {

    @Autowired
    private ClientDaoV1 clientDao;
    
    @Autowired
    private ClientMapperV1 clientMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public PaginatedResponseV1<ClientDtoV1> getAllClients(int page, int size) {
        List<ClientDtoV1> clients = clientDao.findAll(page, size);
        Long totalElements = clientDao.countAll();

        return new PaginatedResponseV1<>(clients, page, size, totalElements);
    }

    @Override
    public ClientDtoV1 getClientById(Long clientId) {
        if (clientId == null || clientId <= 0) 
            throw new BadRequestException("Client ID is required and must be greater than 0");

        ClientDtoV1 dto = clientDao.findDtoById(clientId);
        if (dto == null) throw new NotFoundException("Client not found with ID: " + clientId);

        return dto;
    }

    @Override
    @Transactional
    public ClientDtoV1 createClient(CreateClientDtoV1 createDto) {
        if (createDto == null) throw new BadRequestException("Client data is required");
        
        String username = createDto.getUsername() == null ? null : createDto.getUsername().trim();
        if (username == null || username.isEmpty()) 
            throw new BadRequestException("Username is required");

        if (clientDao.existsByUsername(username)) 
            throw new BadRequestException("Username already exists: " + username);

        ClientV1 client = clientMapper.toEntity(createDto);
        client.setUsername(username);
        client.setPasswordHash(passwordEncoder.encode(createDto.getPassword()));

        ClientV1 savedClient = clientDao.save(client);

        return clientMapper.toDto(savedClient);
    }

    @Override
    @Transactional
    public ClientDtoV1 updateClient(Long clientId, UpdateClientDtoV1 updateDto) {
        if (clientId == null) throw new BadRequestException("Client ID is required");
        if (updateDto == null) throw new BadRequestException("Update data is required");

        ClientV1 client = clientDao.findById(clientId).orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));

        if (updateDto.getUsername() != null && !updateDto.getUsername().trim().isEmpty()) {
            String newUsername = updateDto.getUsername().trim();
            if (!newUsername.equals(client.getUsername()) && clientDao.existsByUsername(newUsername)) 
                throw new BadRequestException("Username already exists: " + newUsername);
            client.setUsername(newUsername);
        }

        clientMapper.updateEntityFromDto(updateDto, client);

        if (updateDto.getPassword() != null && !updateDto.getPassword().trim().isEmpty()) 
            client.setPasswordHash(passwordEncoder.encode(updateDto.getPassword()));

        clientDao.update(client);

        return clientMapper.toDto(client);
    }

    @Override
    @Transactional
    public void deleteClient(Long clientId) {
        if (clientId == null) throw new BadRequestException("Client ID is required");

        ClientV1 client = clientDao.findById(clientId).orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));

        clientDao.delete(client);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return clientDao.existsByUsername(username.trim());
    }

    @Override
    public PaginatedResponseV1<ClientDtoV1> getClientsByRoomId(Long roomId, int page, int size) {
        if (roomId == null || roomId <= 0) 
            throw new BadRequestException("Room ID is required and must be greater than 0");
        
        List<ClientDtoV1> gateways = clientDao.findGatewaysByRoomId(roomId, page, size);
        Long totalElements = clientDao.countGatewaysByRoomId(roomId);
        
        return new PaginatedResponseV1<>(gateways, page, size, totalElements);
    }

    @Override
    public Long countGateWayByRoomId(Long roomId) {
        if (roomId == null || roomId <= 0) 
            throw new BadRequestException("Room ID is required and must be greater than 0");
        
        return clientDao.countGatewaysByRoomId(roomId);
    }
}
