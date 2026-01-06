package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.*;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.TemperatureMapperV1;
import com.iviet.ivshs.service.TemperatureServiceV1;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TemperatureServiceImplV1 implements TemperatureServiceV1 {

    private final TemperatureDao temperatureDao;
    private final RoomDao roomDao;
    private final DeviceControlDao deviceControlDao;
    private final LanguageDao languageDao;
    private final TemperatureMapperV1 temperatureMapper;

    // --- CRUD SENSOR ---

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TemperatureDto> getListByRoom(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<TemperatureDto> data = temperatureDao.findAllByRoomId(roomId, page, size, langCode);
        Long totalElements = temperatureDao.countByRoomId(roomId);

        return new PaginatedResponse<>(data, page, size, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<Temperature> getListEntityByRoom(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
    
        List<Temperature> data = temperatureDao.findAllByRoomId(roomId, page, size);
        Long totalElements = temperatureDao.countByRoomId(roomId);

        return new PaginatedResponse<>(data, page, size, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public TemperatureDto getById(Long tempSensorId) {
        return temperatureDao.findById(tempSensorId, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Temperature sensor not found with ID: " + tempSensorId));
    }

    @Override
    @Transactional(readOnly = true)
    public Temperature getEntityById(Long tempSensorId) {
        if (tempSensorId == null) throw new BadRequestException("Temperature sensor ID is required");

        return temperatureDao.findById(tempSensorId)
                .orElseThrow(() -> new NotFoundException("Temperature sensor not found with ID: " + tempSensorId));
    }

    @Override
    @Transactional(readOnly = true)
    public TemperatureDto getByNaturalId(String naturalId) {
        return temperatureDao.findByNaturalId(naturalId, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Temperature sensor not found with Natural ID: " + naturalId));
    }

    @Override
    @Transactional(readOnly = true)
    public Temperature getEntityByNaturalId(String naturalId) {
        if (naturalId.isBlank()) throw new BadRequestException("Natural ID is required");

        return temperatureDao.findByNaturalId(naturalId)
                .orElseThrow(() -> new NotFoundException("Temperature sensor not found with Natural ID: " + naturalId));
    }

    @Override
    @Transactional
    public TemperatureDto create(CreateTemperatureDto dto) {
        if (dto == null || !StringUtils.hasText(dto.naturalId())) throw new BadRequestException("Data and Natural ID are required");

        Room room = roomDao.findById(dto.roomId()).orElseThrow(() -> new NotFoundException("Room not found"));
        DeviceControl dc = deviceControlDao.findById(dto.deviceControlId()).orElseThrow(() -> new NotFoundException("Device Control not found"));

        if (dc.getRoom() == null || !dc.getRoom().getId().equals(room.getId())) throw new BadRequestException("Device Control does not belong to the specified Room");

        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found");

        Temperature sensor = temperatureMapper.fromCreateDto(dto);
        sensor.setRoom(room);
        sensor.setDeviceControl(dc);

        TemperatureLan lan = new TemperatureLan();
        lan.setLangCode(langCode);
        lan.setName(dto.name().trim());
        lan.setDescription(dto.description());
        lan.setOwner(sensor);

        sensor.getTranslations().add(lan);
        temperatureDao.save(sensor);

        return temperatureMapper.toDto(sensor, lan);
    }

    @Override
    @Transactional
    public TemperatureDto update(Long tempSensorId, UpdateTemperatureDto dto) {
        Temperature sensor = temperatureDao.findById(tempSensorId)
                .orElseThrow(() -> new NotFoundException("Sensor not found"));
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());

        if (dto.isActive() != null) sensor.setIsActive(dto.isActive());

        TemperatureLan lan = sensor.getTranslations().stream()
                .filter(l -> langCode.equals(l.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    TemperatureLan newLan = new TemperatureLan();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(sensor);
                    sensor.getTranslations().add(newLan);
                    return newLan;
                });

        if (dto.name() != null) lan.setName(dto.name().trim());
        if (dto.description() != null) lan.setDescription(dto.description());

        temperatureDao.save(sensor);
        return temperatureMapper.toDto(sensor, lan);
    }

    @Override
    @Transactional
    public void delete(Long tempSensorId) {
        if (!temperatureDao.existsById(tempSensorId)) throw new NotFoundException("Sensor not found");
        temperatureDao.deleteById(tempSensorId);
    }
}