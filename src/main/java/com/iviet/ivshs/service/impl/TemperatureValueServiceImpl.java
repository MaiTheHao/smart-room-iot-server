package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dao.TemperatureDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.CreateTemperatureValueDto;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.entities.TemperatureValue;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.TemperatureValueMapper;
import com.iviet.ivshs.service.TemperatureValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemperatureValueServiceImpl implements TemperatureValueService {

    private final RoomDao roomDao;
    private final TemperatureDao temperatureDao;
    private final TemperatureValueDao temperatureValueDao;
    private final TemperatureValueMapper temperatureValueMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AverageTemperatureValueDto> getAverageTemperatureByRoom(Long roomId, Instant fromTimestamp, Instant toTimestamp) {
        return temperatureValueDao.getAverageHistoryByRoom(
            roomDao.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found with id: " + roomId)).getId()
            , fromTimestamp, toTimestamp);
    }

    @Override
    @Transactional
    public void create(CreateTemperatureValueDto dto) {
        Temperature sensor = temperatureDao.findByNaturalId(dto.sensorNaturalId()).orElseThrow(() -> new NotFoundException("Temperature sensor not found with natural ID: " + dto.sensorNaturalId()));
        TemperatureValue record = temperatureValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        temperatureValueDao.save(record);
    }

    @Override
    @Transactional
    public void createWithSensor(Temperature sensor, CreateTemperatureValueDto dto) {
        TemperatureValue record = temperatureValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        temperatureValueDao.saveAndForget(sensor.getId(), record);
    }

    @Override
    @Transactional
    public void createBatchWithSensor(Temperature sensor, List<CreateTemperatureValueDto> dtoList) {
        temperatureValueDao.saveAndForget(sensor.getId(), dtoList.stream().filter(dto -> dto != null)
            .map(temperatureValueMapper::fromCreateDto)
            .toList()
        );
    }
}