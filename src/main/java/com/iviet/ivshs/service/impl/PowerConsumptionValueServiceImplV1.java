package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.PowerConsumptionDaoV1;
import com.iviet.ivshs.dao.PowerConsumptionValueDaoV1;
import com.iviet.ivshs.dao.RoomDaoV1;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDtoV1;
import com.iviet.ivshs.entities.PowerConsumptionV1;
import com.iviet.ivshs.entities.PowerConsumptionValueV1;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.PowerConsumptionValueMapperV1;
import com.iviet.ivshs.service.PowerConsumptionValueServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerConsumptionValueServiceImplV1 implements PowerConsumptionValueServiceV1 {

    private final RoomDaoV1 roomDao;
    private final PowerConsumptionDaoV1 powerConsumptionDao;
    private final PowerConsumptionValueDaoV1 powerConsumptionValueDao;
    private final PowerConsumptionValueMapperV1 powerConsumptionValueMapper;

    @Override
    public List<SumPowerConsumptionValueDtoV1> getSumPowerConsumptionByRoom(Long roomId, Instant fromTimestamp, Instant toTimestamp) {
        return powerConsumptionValueDao.getSumHistoryByRoom(
            roomDao.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found with id: " + roomId)).getId(), 
            fromTimestamp, 
            toTimestamp
        );
    }

    @Override
    @Transactional
    public void create(CreatePowerConsumptionValueDtoV1 dto) {
        PowerConsumptionV1 sensor = powerConsumptionDao.findByNaturalId(dto.sensorNaturalId()).orElseThrow(() -> new NotFoundException("Power consumption sensor not found with natural ID: " + dto.sensorNaturalId()));
        PowerConsumptionValueV1 record = powerConsumptionValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        powerConsumptionValueDao.save(record);
    }

    @Override
    @Transactional
    public void createWithSensor(PowerConsumptionV1 sensor, CreatePowerConsumptionValueDtoV1 dto) {
        PowerConsumptionValueV1 record = powerConsumptionValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        powerConsumptionValueDao.saveAndForget(sensor.getId(), record);
    }

    @Override
    @Transactional
    public void createBatchWithSensor(PowerConsumptionV1 sensor, List<CreatePowerConsumptionValueDtoV1> dtoList) {
        powerConsumptionValueDao.saveAndForget(sensor.getId(), dtoList.stream()
            .filter(dto -> dto != null)
            .map(powerConsumptionValueMapper::fromCreateDto)
            .toList()
        );
    }
}