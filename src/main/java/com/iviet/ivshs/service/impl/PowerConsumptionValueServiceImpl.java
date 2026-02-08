package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.dao.PowerConsumptionValueDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDto;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDto;
import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.PowerConsumptionValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerConsumptionValueServiceImpl implements PowerConsumptionValueService {

    private final RoomDao roomDao;
    private final PowerConsumptionDao powerConsumptionDao;
    private final PowerConsumptionValueDao powerConsumptionValueDao;

    @Override
    public List<SumPowerConsumptionValueDto> getSumPowerConsumptionByRoom(Long roomId, Instant fromTimestamp, Instant toTimestamp) {
        return powerConsumptionValueDao.getSumHistoryByRoom(
            roomDao.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found with id: " + roomId)).getId(), 
            fromTimestamp, 
            toTimestamp
        );
    }

    @Override
    @Transactional
    public void create(CreatePowerConsumptionValueDto dto) {
        var sensor = powerConsumptionDao.findByNaturalId(dto.sensorNaturalId()).orElseThrow(() -> new NotFoundException("Power consumption sensor not found with natural ID: " + dto.sensorNaturalId()));
        var record = dto.toEntity();
        record.setSensor(sensor);
        powerConsumptionValueDao.save(record);
    }

    @Override
    @Transactional
    public void createWithSensor(PowerConsumption sensor, CreatePowerConsumptionValueDto dto) {
        var record = dto.toEntity();
        record.setSensor(sensor);
        powerConsumptionValueDao.saveAndForget(sensor.getId(), record);
    }

    @Override
    @Transactional
    public void createBatchWithSensor(PowerConsumption sensor, List<CreatePowerConsumptionValueDto> dtoList) {
        powerConsumptionValueDao.saveAndForget(sensor.getId(), dtoList.stream()
            .filter(dto -> dto != null)
            .map(CreatePowerConsumptionValueDto::toEntity)
            .toList()
        );
    }
}