package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dao.TemperatureDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.CreateTemperatureValueDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.NotFoundException;
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

  @Override
  public DeviceCategory getSupportedCategory() {
    return DeviceCategory.TEMPERATURE;
  }

  @Override
  public void create(TelemetryResponseDto.Data data) {
    Double tempC = data.getData().get("tempC") != null ? data.getData().get("tempC").asDouble() : null;
    if (tempC == null) return;

    var sensor = temperatureDao.findByNaturalId(data.getNaturalId()).orElseThrow(() -> new NotFoundException("Temperature sensor not found with natural ID: " + data.getNaturalId()));
    var record = CreateTemperatureValueDto.builder()
        .sensorNaturalId(data.getNaturalId())
        .tempC(tempC)
        .timestamp(Instant.now())
        .build()
        .toEntity();
        
    record.setSensor(sensor);
    temperatureValueDao.save(record);
  }

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
    if (dto.tempC() == null) return;
    
    var sensor = temperatureDao.findByNaturalId(dto.sensorNaturalId()).orElseThrow(() -> new NotFoundException("Temperature sensor not found with natural ID: " + dto.sensorNaturalId()));
    var record = dto.toEntity();
    record.setSensor(sensor);
    temperatureValueDao.save(record);

    sensor.setCurrentValue(record.getTempC());
    temperatureDao.save(sensor);
  }

  @Override
  @Transactional
  public void createWithSensor(Temperature sensor, CreateTemperatureValueDto dto) {
    if (dto.tempC() == null) return;

    var record = dto.toEntity();
    record.setSensor(sensor);
    temperatureValueDao.saveAndForget(sensor.getId(), record);

    sensor.setCurrentValue(record.getTempC());
    temperatureDao.save(sensor);
  }

  @Override
  @Transactional
  public void createBatchWithSensor(Temperature sensor, List<CreateTemperatureValueDto> dtoList) {
    if (dtoList == null || dtoList.isEmpty()) {
      return;
    }

    List<CreateTemperatureValueDto> sortedByTimestampLatestFirst = dtoList.stream()
        .filter(dto -> dto != null && dto.tempC() != null)
        .sorted((dto1, dto2) -> dto2.timestamp().compareTo(dto1.timestamp()))
        .toList();

    if (sortedByTimestampLatestFirst.isEmpty()) {
      return;
    }

    temperatureValueDao.saveAndForget(sensor.getId(), sortedByTimestampLatestFirst.stream()
        .map(CreateTemperatureValueDto::toEntity)
        .toList()
    );

    sensor.setCurrentValue(sortedByTimestampLatestFirst.get(0).tempC());
    temperatureDao.save(sensor);
  }
}