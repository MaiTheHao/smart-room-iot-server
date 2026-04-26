package com.iviet.ivshs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.HardwareConfigDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.CreateDeviceControlDto;
import com.iviet.ivshs.dto.DeviceControlDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateDeviceControlDto;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.HardwareConfigService;

@Service
@Transactional(readOnly = true)
public class HardwareConfigServiceImpl implements HardwareConfigService {

    @Autowired
    private HardwareConfigDao deviceControlDao;
    
    @Autowired
    private ClientDao clientDao;
    
    @Autowired
    private RoomDao roomDao;

    @Override
    public DeviceControlDto getById(Long deviceControlId) {
        if (deviceControlId == null) 
            throw new BadRequestException("Device Control ID is required");
        
        var hardwareConfig = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));
        
        return DeviceControlDto.from(hardwareConfig);
    }

    @Override
    @Transactional
    public DeviceControlDto create(CreateDeviceControlDto dto) {
        if (dto == null) throw new BadRequestException("Device control data is required");
        if (dto.clientId() == null) throw new BadRequestException("Client ID is required");
        if (dto.roomId() == null) throw new BadRequestException("Room ID is required");

        var client = clientDao.findById(dto.clientId()).orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.clientId()));
        
        var room = roomDao.findById(dto.roomId()).orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));

        var hardwareConfig = dto.toEntity();
        hardwareConfig.setClient(client);
        hardwareConfig.setRoom(room);

        var savedDeviceControl = deviceControlDao.save(hardwareConfig);

        return DeviceControlDto.from(savedDeviceControl);
    }

    @Override
    @Transactional
    public DeviceControlDto update(Long deviceControlId, UpdateDeviceControlDto dto) {
        if (deviceControlId == null) throw new BadRequestException("Device Control ID is required");
        if (dto == null) throw new BadRequestException("Update data is required");

        var hardwareConfig = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));

        if (dto.clientId() != null) {
            Long currentClientId = hardwareConfig.getClient() != null ? hardwareConfig.getClient().getId() : null;
            if (!dto.clientId().equals(currentClientId)) {
                var client = clientDao.findById(dto.clientId()).orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.clientId()));
                hardwareConfig.setClient(client);
            }
        }

        if (dto.roomId() != null && !dto.roomId().equals(hardwareConfig.getRoom().getId())) {
            var room = roomDao.findById(dto.roomId()).orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));
            hardwareConfig.setRoom(room);
        }

        dto.applyTo(hardwareConfig);
        deviceControlDao.update(hardwareConfig);

        return DeviceControlDto.from(hardwareConfig);
    }

    @Override
    @Transactional
    public void delete(Long deviceControlId) {
        if (deviceControlId == null) throw new BadRequestException("Device Control ID is required");

        HardwareConfig hardwareConfig = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));
        
        deviceControlDao.delete(hardwareConfig);
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
