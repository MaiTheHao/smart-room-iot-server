package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.dao.PowerConsumptionValueDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDto;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.PowerConsumptionValueService;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PowerConsumptionValueServiceImpl implements PowerConsumptionValueService {

  private final RoomDao roomDao;
  private final PowerConsumptionDao powerConsumptionDao;
  private final PowerConsumptionValueDao powerConsumptionValueDao;
  
  @Override
  public DeviceCategory getSupportedCategory() {
    return DeviceCategory.POWER_CONSUMPTION;
  }

  @Override
  @Transactional
  public void create(TelemetryResponseDto.Data data) {
    JsonNode wattNode = data.getData().get("watt");
    if (wattNode == null || !wattNode.isNumber()) return;
    Double watt = wattNode.asDouble();

    var sensor = powerConsumptionDao.findByNaturalId(data.getNaturalId()).orElseThrow(() -> new NotFoundException("Power consumption sensor not found with natural ID: " + data.getNaturalId()));
    var record = CreatePowerConsumptionValueDto.builder()
        .sensorNaturalId(data.getNaturalId())
        .watt(watt)
        .timestamp(Instant.now())
        .build()
        .toEntity();
        
    record.setSensor(sensor);
    powerConsumptionValueDao.save(record);
    sensor.setCurrentWatt(record.getWatt());
  }

  @Override
  public List<SumPowerConsumptionValueDto> getSumPowerConsumptionByRoom(Long roomId, Instant fromTimestamp, Instant toTimestamp) {
    int divisor = TelemetryTimeGroup.getDivisorForRange(fromTimestamp, toTimestamp);
    return powerConsumptionValueDao.getSumHistoryByRoom(
        roomDao.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found with id: " + roomId)).getId(),
        fromTimestamp,
        toTimestamp,
        divisor
    );
  }

  @Override
  @Transactional
  public void create(CreatePowerConsumptionValueDto dto) {
    if (dto.watt() == null) return;
    
    var sensor = powerConsumptionDao.findByNaturalId(dto.sensorNaturalId()).orElseThrow(() -> new NotFoundException("Power consumption sensor not found with natural ID: " + dto.sensorNaturalId()));
    var record = dto.toEntity();
    record.setSensor(sensor);
    powerConsumptionValueDao.save(record);

    sensor.setCurrentWatt(record.getWatt());
    powerConsumptionDao.save(sensor);
  }

  @Override
  @Transactional
  public void createWithSensor(PowerConsumption sensor, CreatePowerConsumptionValueDto dto) {
    if (dto.watt() == null) return;

    var record = dto.toEntity();
    record.setSensor(sensor);
    powerConsumptionValueDao.saveAndForget(sensor.getId(), record);

    sensor.setCurrentWatt(record.getWatt());
    powerConsumptionDao.save(sensor);
  }

  @Override
  @Transactional
  public void createBatchWithSensor(PowerConsumption sensor, List<CreatePowerConsumptionValueDto> dtoList) {
    if (dtoList == null || dtoList.isEmpty()) {
      return;
    }

    List<CreatePowerConsumptionValueDto> sortedByTimestampLatestFirst = dtoList.stream()
        .filter(dto -> dto != null && dto.watt() != null)
        .sorted((dto1, dto2) -> dto2.timestamp().compareTo(dto1.timestamp()))
        .toList();

    if (sortedByTimestampLatestFirst.isEmpty()) {
      return;
    }

    powerConsumptionValueDao.saveAndForget(sensor.getId(), sortedByTimestampLatestFirst.stream()
        .map(CreatePowerConsumptionValueDto::toEntity)
        .toList()
    );

    sensor.setCurrentWatt(sortedByTimestampLatestFirst.get(0).watt());
    powerConsumptionDao.save(sensor);
  }
}