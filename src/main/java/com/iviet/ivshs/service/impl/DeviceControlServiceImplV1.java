package com.iviet.ivshs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.ClientDaoV1;
import com.iviet.ivshs.dao.DeviceControlDaoV1;
import com.iviet.ivshs.dao.RoomDaoV1;
import com.iviet.ivshs.dto.CreateDeviceControlDtoV1;
import com.iviet.ivshs.dto.DeviceControlDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.UpdateDeviceControlDtoV1;
import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.entities.DeviceControlV1;
import com.iviet.ivshs.entities.RoomV1;
import com.iviet.ivshs.exception.BadRequestException;
import com.iviet.ivshs.exception.NotFoundException;
import com.iviet.ivshs.mapper.DeviceControlMapperV1;
import com.iviet.ivshs.service.DeviceControlServiceV1;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DeviceControlServiceImplV1 implements DeviceControlServiceV1 {

    @Autowired
    private DeviceControlDaoV1 deviceControlDao;
    
    @Autowired
    private DeviceControlMapperV1 deviceControlMapper;
    
    @Autowired
    private ClientDaoV1 clientDao;
    
    @Autowired
    private RoomDaoV1 RoomDaoV1;

    @Override
    public DeviceControlDtoV1 getById(Long deviceControlId) {
        if (deviceControlId == null) 
            throw new BadRequestException("Device Control ID is required");
        
        DeviceControlV1 deviceControl = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));
        
        return deviceControlMapper.toDto(deviceControl);
    }

    @Override
    @Transactional
    public DeviceControlDtoV1 create(CreateDeviceControlDtoV1 dto) {
        if (dto == null) throw new BadRequestException("Device control data is required");
        if (dto.getClientId() == null) throw new BadRequestException("Client ID is required");
        if (dto.getRoomId() == null) throw new BadRequestException("Room ID is required");

        ClientV1 client = clientDao.findById(dto.getClientId()).orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.getClientId()));
        
        RoomV1 room = RoomDaoV1.findById(dto.getRoomId()).orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.getRoomId()));

        DeviceControlV1 deviceControl = deviceControlMapper.toEntity(dto);
        deviceControl.setClient(client);
        deviceControl.setRoom(room);

        DeviceControlV1 savedDeviceControl = deviceControlDao.save(deviceControl);

        return deviceControlMapper.toDto(savedDeviceControl);
    }

    @Override
    @Transactional
    public DeviceControlDtoV1 update(Long deviceControlId, UpdateDeviceControlDtoV1 dto) {
        if (deviceControlId == null) throw new BadRequestException("Device Control ID is required");
        if (dto == null) throw new BadRequestException("Update data is required");

        DeviceControlV1 deviceControl = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));

        if (dto.getClientId() != null) {
            Long currentClientId = deviceControl.getClient() != null ? deviceControl.getClient().getId() : null;
            if (!dto.getClientId().equals(currentClientId)) {
                ClientV1 client = clientDao.findById(dto.getClientId()).orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.getClientId()));
                deviceControl.setClient(client);
            }
        }

        if (dto.getRoomId() != null && !dto.getRoomId().equals(deviceControl.getRoom().getId())) {
            RoomV1 room = RoomDaoV1.findById(dto.getRoomId()).orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.getRoomId()));
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

        DeviceControlV1 deviceControl = deviceControlDao.findById(deviceControlId).orElseThrow(() -> new NotFoundException("Device control not found with ID: " + deviceControlId));
        
        deviceControlDao.delete(deviceControl);
    }

    @Override
    public PaginatedResponseV1<DeviceControlDtoV1> getListByClientId(Long clientId, int page, int size) {
        if (clientId == null) throw new BadRequestException("Client ID is required");
        
        List<DeviceControlV1> deviceControls = deviceControlDao.findByClientId(clientId, page, size);
        List<DeviceControlDtoV1> content = deviceControlMapper.toListDto(deviceControls);
        Long totalElements = deviceControlDao.countByClientId(clientId);
        
        return new PaginatedResponseV1<>(content, page, size, totalElements);
    }

    @Override
    public PaginatedResponseV1<DeviceControlDtoV1> getListByRoomId(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        
        List<DeviceControlV1> deviceControls = deviceControlDao.findByRoomId(roomId, page, size);
        List<DeviceControlDtoV1> content = deviceControlMapper.toListDto(deviceControls);
        Long totalElements = deviceControlDao.countByRoomId(roomId);
        
        return new PaginatedResponseV1<>(content, page, size, totalElements);
    }

    @Override
    public Long countByRoomId(Long roomId) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        return deviceControlDao.countByRoomId(roomId);
    }
}
