package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.RoomDaoV1;
import com.iviet.ivshs.dao.TemperatureDaoV1;
import com.iviet.ivshs.dao.TemperatureValueDaoV1;
import com.iviet.ivshs.dto.AverageTemperatureValueDtoV1;
import com.iviet.ivshs.dto.CreateTemperatureValueDtoV1;
import com.iviet.ivshs.entities.TemperatureV1;
import com.iviet.ivshs.entities.TemperatureValueV1;
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

    private final RoomDaoV1 roomDao;
    private final TemperatureDaoV1 temperatureDao;
    private final TemperatureValueDaoV1 temperatureValueDao;
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
        TemperatureV1 sensor = temperatureDao.findByNaturalId(dto.sensorNaturalId()).orElseThrow(() -> new NotFoundException("Temperature sensor not found with natural ID: " + dto.sensorNaturalId()));
        TemperatureValueV1 record = temperatureValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        temperatureValueDao.save(record);
    }

    @Override
    @Transactional
    public void createWithSensor(TemperatureV1 sensor, CreateTemperatureValueDtoV1 dto) {
        TemperatureValueV1 record = temperatureValueMapper.fromCreateDto(dto);
        record.setSensor(sensor);
        temperatureValueDao.saveAndForget(sensor.getId(), record);
    }

    @Override
    @Transactional
    public void createBatchWithSensor(TemperatureV1 sensor, List<CreateTemperatureValueDtoV1> dtoList) {
        temperatureValueDao.saveAndForget(sensor.getId(), dtoList.stream().filter(dto -> dto != null)
            .map(temperatureValueMapper::fromCreateDto)
            .toList()
        );
    }
}