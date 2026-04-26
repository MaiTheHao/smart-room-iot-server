package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.*;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.TemperatureService;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TemperatureServiceImpl implements TemperatureService {

    private final RoomDao roomDao;
    private final LanguageDao languageDao;
    private final TemperatureDao temperatureDao;
    private final HardwareConfigDao deviceControlDao;

    // --- CRUD SENSOR ---

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TemperatureDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<TemperatureDto> data = temperatureDao.findAll(page, size, langCode);
        Long totalElements = temperatureDao.count();

        return new PaginatedResponse<>(data, page, size, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemperatureDto> getAll() {
        return temperatureDao.findAll(LocalContextUtil.getCurrentLangCode());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TemperatureDto> getListByRoomId(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<TemperatureDto> data = temperatureDao.findAllByRoomId(roomId, page, size, langCode);
        Long totalElements = temperatureDao.countByRoomId(roomId);

        return new PaginatedResponse<>(data, page, size, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemperatureDto> getAllByRoomId(Long roomId) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        return temperatureDao.findAllByRoomId(roomId, LocalContextUtil.getCurrentLangCode());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<Temperature> getListEntityByRoomId(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
    
        List<Temperature> data = temperatureDao.findAllByRoomId(roomId, page, size);
        Long totalElements = temperatureDao.countByRoomId(roomId);

        return new PaginatedResponse<>(data, page, size, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public TemperatureDto getById(Long id) {
        return temperatureDao.findById(id, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Temperature sensor not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Temperature getEntityById(Long id) {
        if (id == null) throw new BadRequestException("Temperature sensor ID is required");

        return temperatureDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Temperature sensor not found with ID: " + id));
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

        var room = roomDao.findById(dto.roomId()).orElseThrow(() -> new NotFoundException("Room not found"));
        var dc = deviceControlDao.findById(dto.deviceControlId()).orElseThrow(() -> new NotFoundException("Device Control not found"));

        if (dc.getRoom() == null || !dc.getRoom().getId().equals(room.getId())) throw new BadRequestException("Device Control does not belong to the specified Room");

        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found");

        var sensor = dto.toEntity();
        sensor.setRoom(room);
        sensor.setHardwareConfig(dc);

        var lan = new TemperatureLan();
        lan.setLangCode(langCode);
        lan.setName(dto.name().trim());
        lan.setDescription(dto.description());
        lan.setOwner(sensor);

        sensor.getTranslations().add(lan);
        sensor.touch();
        temperatureDao.save(sensor);
        temperatureDao.flush();

        return TemperatureDto.from(sensor, lan);
    }

    @Override
    @Transactional
    public TemperatureDto update(Long id, UpdateTemperatureDto dto) {
        var sensor = temperatureDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Sensor not found"));
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());

        if (dto.isActive() != null) sensor.setIsActive(dto.isActive());

        var lan = sensor.getTranslations().stream()
                .filter(l -> langCode.equals(l.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    var newLan = new TemperatureLan();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(sensor);
                    sensor.getTranslations().add(newLan);
                    return newLan;
                });

        if (dto.name() != null) lan.setName(dto.name().trim());
        if (dto.description() != null) lan.setDescription(dto.description());

        sensor.touch();
        temperatureDao.save(sensor);
        temperatureDao.flush();
        return TemperatureDto.from(sensor, lan);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!temperatureDao.existsById(id)) throw new NotFoundException("Sensor not found");
        Temperature target = temperatureDao.findById(id).orElseThrow(() -> new NotFoundException("Sensor not found"));
        HardwareConfig targetDeviceControl = target.getHardwareConfig();
        deviceControlDao.delete(targetDeviceControl);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByRoomId(Long roomId) {
        return temperatureDao.countByRoomId(roomId);
    }
}