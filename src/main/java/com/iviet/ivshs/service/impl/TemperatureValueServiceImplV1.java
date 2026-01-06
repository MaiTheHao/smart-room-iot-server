package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dao.TemperatureDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.AverageTemperatureValueDtoV1;
import com.iviet.ivshs.dto.CreateTemperatureValueDtoV1;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.entities.TemperatureValue;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.TemperatureValueMapperV1;
import com.iviet.ivshs.service.TemperatureValueServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemperatureValueServiceImplV1 implements TemperatureValueServiceV1 {

    private final RoomDao roomDao;
    private final TemperatureDao temperatureDao;
    private final TemperatureValueDao temperatureValueDao;
    private final TemperatureValueMapperV1 temperatureValueMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AverageTemperatureValueDtoV1> getAverageTemperatureByRoom(Long roomId, Instant fromTimestamp, Instant toTimestamp) {
        return temperatureValueDao.getAverageHistoryByRoom(
            roomDao.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found with id: " + roomId)).getId()
            , fromTimestamp, toTimestamp);
    }

    @Override
    @Transactional
    public void create(CreateTemperatureValueDtoV1 dto) {
        Temperature sensor = temperatureDao.findByNaturalId(dto.sensorNaturalId()).orElseThrow(() -> new NotFoundException("Temperature sensor not found with natural ID: " + dto.sensorNaturalId()));
        TemperatureValue record = temperatureValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        temperatureValueDao.save(record);
    }

    @Override
    @Transactional
    public void createWithSensor(Temperature sensor, CreateTemperatureValueDtoV1 dto) {
        TemperatureValue record = temperatureValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        temperatureValueDao.saveAndForget(sensor.getId(), record);
    }

    @Override
    @Transactional
    public void createBatchWithSensor(Temperature sensor, List<CreateTemperatureValueDtoV1> dtoList) {
        temperatureValueDao.saveAndForget(sensor.getId(), dtoList.stream().filter(dto -> dto != null)
            .map(temperatureValueMapper::fromCreateDto)
            .toList()
        );
    }
}