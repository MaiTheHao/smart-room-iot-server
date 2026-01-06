package com.iviet.ivshs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.DeviceControlDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.CreateDeviceControlDto;
import com.iviet.ivshs.dto.DeviceControlDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateDeviceControlDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.DeviceControlMapperV1;
import com.iviet.ivshs.service.DeviceControlServiceV1;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DeviceControlServiceImplV1 implements DeviceControlServiceV1 {

    @Autowired
    private DeviceControlDao deviceControlDao;
    
    @Autowired
    private DeviceControlMapperV1 deviceControlMapper;
    
    @Autowired
    private ClientDao clientDao;
    
    @Autowired
    private RoomDao RoomDaoV1;

    @Override
    public DeviceControlDto getById(Long deviceControlId) {
        if (deviceControlId == null) 
            throw new BadRequestException("Device Control ID is required");
        
        DeviceControl deviceControl = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));
        
        return deviceControlMapper.toDto(deviceControl);
    }

    @Override
    @Transactional
    public DeviceControlDto create(CreateDeviceControlDto dto) {
        if (dto == null) throw new BadRequestException("Device control data is required");
        if (dto.getClientId() == null) throw new BadRequestException("Client ID is required");
        if (dto.getRoomId() == null) throw new BadRequestException("Room ID is required");

        Client client = clientDao.findById(dto.getClientId()).orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.getClientId()));
        
        Room room = RoomDaoV1.findById(dto.getRoomId()).orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.getRoomId()));

        DeviceControl deviceControl = deviceControlMapper.toEntity(dto);
        deviceControl.setClient(client);
        deviceControl.setRoom(room);

        DeviceControl savedDeviceControl = deviceControlDao.save(deviceControl);

        return deviceControlMapper.toDto(savedDeviceControl);
    }

    @Override
    @Transactional
    public DeviceControlDto update(Long deviceControlId, UpdateDeviceControlDto dto) {
        if (deviceControlId == null) throw new BadRequestException("Device Control ID is required");
        if (dto == null) throw new BadRequestException("Update data is required");

        DeviceControl deviceControl = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));

        if (dto.getClientId() != null) {
            Long currentClientId = deviceControl.getClient() != null ? deviceControl.getClient().getId() : null;
            if (!dto.getClientId().equals(currentClientId)) {
                Client client = clientDao.findById(dto.getClientId()).orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.getClientId()));
                deviceControl.setClient(client);
            }
        }

        if (dto.getRoomId() != null && !dto.getRoomId().equals(deviceControl.getRoom().getId())) {
            Room room = RoomDaoV1.findById(dto.getRoomId()).orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.getRoomId()));
            deviceControl.setRoom(room);
        }

        deviceControlMapper.updateEntityFromDto(dto, deviceControl);
        deviceControlDao.update(deviceControl);

        return deviceControlMapper.toDto(deviceControl);
    }

    @Override
    @Transactional
    public void delete(Long deviceControlId) {
        if (deviceControlId == null) throw new BadRequestException("Device Control ID is required");

        DeviceControl deviceControl = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));
        
        deviceControlDao.delete(deviceControl);
    }

    @Override
    public PaginatedResponse<DeviceControlDto> getListByClientId(Long clientId, int page, int size) {
        if (clientId == null) throw new BadRequestException("Client ID is required");
        
        List<DeviceControl> deviceControls = deviceControlDao.findByClientId(clientId, page, size);
        List<DeviceControlDto> content = deviceControlMapper.toListDto(deviceControls);
        Long totalElements = deviceControlDao.countByClientId(clientId);
        
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    @Override
    public PaginatedResponse<DeviceControlDto> getListByRoomId(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        
        List<DeviceControl> deviceControls = deviceControlDao.findByRoomId(roomId, page, size);
        List<DeviceControlDto> content = deviceControlMapper.toListDto(deviceControls);
        Long totalElements = deviceControlDao.countByRoomId(roomId);
        
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    @Override
    public Long countByRoomId(Long roomId) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        return deviceControlDao.countByRoomId(roomId);
    }
}
