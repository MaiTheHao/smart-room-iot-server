package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.dao.PowerConsumptionValueDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDtoV1;
import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.entities.PowerConsumptionValue;
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

    private final RoomDao roomDao;
    private final PowerConsumptionDao powerConsumptionDao;
    private final PowerConsumptionValueDao powerConsumptionValueDao;
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
        PowerConsumption sensor = powerConsumptionDao.findByNaturalId(dto.sensorNaturalId()).orElseThrow(() -> new NotFoundException("Power consumption sensor not found with natural ID: " + dto.sensorNaturalId()));
        PowerConsumptionValue record = powerConsumptionValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        powerConsumptionValueDao.save(record);
    }

    @Override
    @Transactional
    public void createWithSensor(PowerConsumption sensor, CreatePowerConsumptionValueDtoV1 dto) {
        PowerConsumptionValue record = powerConsumptionValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        powerConsumptionValueDao.saveAndForget(sensor.getId(), record);
    }

    @Override
    @Transactional
    public void createBatchWithSensor(PowerConsumption sensor, List<CreatePowerConsumptionValueDtoV1> dtoList) {
        powerConsumptionValueDao.saveAndForget(sensor.getId(), dtoList.stream()
            .filter(dto -> dto != null)
            .map(powerConsumptionValueMapper::fromCreateDto)
            .toList()
        );
    }
}