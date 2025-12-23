package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.*;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.enumeration.GatewayCommandV1;
import com.iviet.ivshs.exception.BadRequestException;
import com.iviet.ivshs.exception.NotFoundException;
import com.iviet.ivshs.mapper.TemperatureMapperV1;
import com.iviet.ivshs.mapper.TemperatureValueMapperV1;
import com.iviet.ivshs.service.HealthCheckServiceV1;
import com.iviet.ivshs.service.TemperatureServiceV1;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemperatureServiceImplV1 implements TemperatureServiceV1 {
    private final TemperatureDaoV1 temperatureDao;
    private final TemperatureValueDaoV1 temperatureValueDao;
    private final RoomDaoV1 roomDao;
    private final DeviceControlDaoV1 deviceControlDao;
    private final LanguageDaoV1 languageDao;
    
    private final TemperatureMapperV1 temperatureMapper;
    private final TemperatureValueMapperV1 temperatureValueMapper;
    
    private final HealthCheckServiceV1 healthCheckService;
    private final PlatformTransactionManager transactionManager;

    // --- CRUD SENSOR ---

    @Override
    public PaginatedResponseV1<TemperatureDtoV1> getListByRoom(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponseV1<>(
                temperatureDao.findAllByRoomId(roomId, page, size, langCode),
                page, size, temperatureDao.countByRoomId(roomId)
        );
    }

    @Override
    public TemperatureDtoV1 getById(Long tempSensorId) {
        return temperatureDao.findById(tempSensorId, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Temperature sensor not found with ID: " + tempSensorId));
    }

    @Override
    @Transactional
    public TemperatureDtoV1 create(CreateTemperatureDtoV1 dto) {
        if (dto == null || !StringUtils.hasText(dto.naturalId())) 
            throw new BadRequestException("Data and Natural ID are required");

        _checkDuplicate(dto.naturalId().trim(), null);

        RoomV1 room = roomDao.findById(dto.roomId()).orElseThrow(() -> new NotFoundException("Room not found"));
        DeviceControlV1 dc = deviceControlDao.findById(dto.deviceControlId()).orElseThrow(() -> new NotFoundException("Device Control not found"));

        if (dc.getRoom() == null || !dc.getRoom().getId().equals(room.getId())) throw new BadRequestException("Device Control does not belong to the specified Room");

        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found");

        TemperatureV1 sensor = temperatureMapper.fromCreateDto(dto);
        sensor.setRoom(room);
        sensor.setDeviceControl(dc);

        TemperatureLanV1 lan = new TemperatureLanV1();
        lan.setLangCode(langCode);
        lan.setName(dto.name().trim());
        lan.setDescription(dto.description());
        lan.setSensor(sensor);

        sensor.getSensorLans().add(lan);
        temperatureDao.save(sensor);

        return temperatureMapper.toDto(sensor, lan);
    }

    @Override
    @Transactional
    public TemperatureDtoV1 update(Long tempSensorId, UpdateTemperatureDtoV1 dto) {
        TemperatureV1 sensor = temperatureDao.findById(tempSensorId)
                .orElseThrow(() -> new NotFoundException("Sensor not found"));
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());

        if (dto.isActive() != null) sensor.setIsActive(dto.isActive());

        TemperatureLanV1 lan = sensor.getSensorLans().stream()
                .filter(l -> langCode.equals(l.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    TemperatureLanV1 newLan = new TemperatureLanV1();
                    newLan.setLangCode(langCode);
                    newLan.setSensor(sensor);
                    sensor.getSensorLans().add(newLan);
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

    // --- DATA INGESTION (VALUES) ---

    @Override
    @Transactional
    public void ingestSensorData(Long sensorId, CreateTemperatureValueDtoV1 dto) {
        TemperatureV1 sensor = temperatureDao.findById(sensorId)
                .orElseThrow(() -> new NotFoundException("Sensor not found"));

        TemperatureValueV1 value = temperatureValueMapper.fromCreateDto(dto);
        value.setSensor(sensor);
        
        temperatureValueDao.save(value);
        
        sensor.setCurrentValue(dto.tempC());
        temperatureDao.save(sensor);
    }

    @Override
    @Transactional
    public void ingestSensorDataBatch(Long sensorId, List<CreateTemperatureValueDtoV1> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        TemperatureV1 sensor = temperatureDao.findById(sensorId)
                .orElseThrow(() -> new NotFoundException("Sensor not found"));

        List<TemperatureValueV1> values = dtos.stream().map(dto -> {
            TemperatureValueV1 val = temperatureValueMapper.fromCreateDto(dto);
            val.setSensor(sensor);
            return val;
        }).collect(Collectors.toList());

        temperatureValueDao.saveAll(values);

        Double lastValue = dtos.get(dtos.size() - 1).tempC();
        sensor.setCurrentValue(lastValue);
        temperatureDao.save(sensor);
    }

    // --- HISTORY & STATS ---

    @Override
    public List<AverageTemperatureValueDtoV1> getAverageValueHistoryByRoomId(Long roomId, Instant startedAt, Instant endedAt) {
        if (startedAt.isAfter(endedAt)) throw new BadRequestException("Start time must be before End time");
        return temperatureValueDao.getAverageHistoryByRoom(roomId, startedAt, endedAt);
    }

    @Override
    public List<AverageTemperatureValueDtoV1> getAverageValueHistoryByClientId(Long clientId, Instant startedAt, Instant endedAt) {
        if (startedAt.isAfter(endedAt)) throw new BadRequestException("Start time must be before End time");
        return temperatureValueDao.getAverageHistoryByClient(clientId, startedAt, endedAt);
    }

    @Override
    @Transactional
    public int cleanupDataByRange(Long sensorId, Instant startedAt, Instant endedAt) {
        if (!temperatureDao.existsById(sensorId)) throw new NotFoundException("Sensor not found");
        return temperatureValueDao.deleteByTimestampBetween(startedAt, endedAt);
    }

    // --- HEALTH CHECK ---

    @Override
    public HealthCheckResponseDtoV1 healthCheck(Long sensorId) {
        TransactionTemplate tm = new TransactionTemplate(transactionManager);
        HealthCheckRequestDtoV1 reqDto = tm.execute(status -> {
            TemperatureV1 sensor = temperatureDao.findById(sensorId)
                    .orElseThrow(() -> new NotFoundException("Sensor not found"));
            
            DeviceControlV1 dc = sensor.getDeviceControl();
            if (dc == null) throw new BadRequestException("No Device Control associated");

            return HealthCheckRequestDtoV1.builder()
                    .deviceControlType(dc.getDeviceControlType())
                    .clientId(dc.getClient().getId())
                    .clientIpAddress(dc.getClient().getIpAddress())
                    .gpioPin(dc.getGpioPin())
                    .command(GatewayCommandV1.HEALTH_CHECK.toString())
                    .build();
        });
        return healthCheckService.check(reqDto);
    }

    // --- HELPER ---
    private void _checkDuplicate(String nid, Long cid) {
        temperatureDao.findByNaturalId(nid).ifPresent(e -> {
            if (cid == null || !e.getId().equals(cid)) {
                throw new BadRequestException("Natural ID already exists: " + nid);
            }
        });
    }
}