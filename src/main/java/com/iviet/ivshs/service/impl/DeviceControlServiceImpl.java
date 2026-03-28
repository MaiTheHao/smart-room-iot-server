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
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.DeviceControlService;

@Service
@Transactional(readOnly = true)
public class DeviceControlServiceImpl implements DeviceControlService {

    @Autowired
    private DeviceControlDao deviceControlDao;
    
    @Autowired
    private ClientDao clientDao;
    
    @Autowired
    private RoomDao roomDao;

    @Override
    public DeviceControlDto getById(Long deviceControlId) {
        if (deviceControlId == null) 
            throw new BadRequestException("Device Control ID is required");
        
        var deviceControl = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));
        
        return DeviceControlDto.from(deviceControl);
    }

    @Override
    @Transactional
    public DeviceControlDto create(CreateDeviceControlDto dto) {
        if (dto == null) throw new BadRequestException("Device control data is required");
        if (dto.clientId() == null) throw new BadRequestException("Client ID is required");
        if (dto.roomId() == null) throw new BadRequestException("Room ID is required");

        var client = clientDao.findById(dto.clientId()).orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.clientId()));
        
        var room = roomDao.findById(dto.roomId()).orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));

        var deviceControl = dto.toEntity();
        deviceControl.setClient(client);
        deviceControl.setRoom(room);

        var savedDeviceControl = deviceControlDao.save(deviceControl);

        return DeviceControlDto.from(savedDeviceControl);
    }

    @Override
    @Transactional
    public DeviceControlDto update(Long deviceControlId, UpdateDeviceControlDto dto) {
        if (deviceControlId == null) throw new BadRequestException("Device Control ID is required");
        if (dto == null) throw new BadRequestException("Update data is required");

        var deviceControl = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));

        if (dto.clientId() != null) {
            Long currentClientId = deviceControl.getClient() != null ? deviceControl.getClient().getId() : null;
            if (!dto.clientId().equals(currentClientId)) {
                var client = clientDao.findById(dto.clientId()).orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.clientId()));
                deviceControl.setClient(client);
            }
        }

        if (dto.roomId() != null && !dto.roomId().equals(deviceControl.getRoom().getId())) {
            var room = roomDao.findById(dto.roomId()).orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));
            deviceControl.setRoom(room);
        }

        dto.applyTo(deviceControl);
        deviceControlDao.update(deviceControl);

        return DeviceControlDto.from(deviceControl);
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
        
        var deviceControls = deviceControlDao.findByClientId(clientId, page, size);
        var content = deviceControls.stream().map(DeviceControlDto::from).toList();
        Long totalElements = deviceControlDao.countByClientId(clientId);
        
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    @Override
    public PaginatedResponse<DeviceControlDto> getListByRoomId(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        
        var deviceControls = deviceControlDao.findByRoomId(roomId, page, size);
        var content = deviceControls.stream().map(DeviceControlDto::from).toList();
        Long totalElements = deviceControlDao.countByRoomId(roomId);
        
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    @Override
    public Long countByRoomId(Long roomId) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        return deviceControlDao.countByRoomId(roomId);
    }
}
