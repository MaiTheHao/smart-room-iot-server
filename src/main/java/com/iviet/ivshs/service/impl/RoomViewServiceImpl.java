package com.iviet.ivshs.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.RoomDetailViewModel;
import com.iviet.ivshs.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.service.HealthCheckService;
import com.iviet.ivshs.service.RoomService;
import com.iviet.ivshs.service.RoomViewService;
import com.iviet.ivshs.properties.EngineProperties;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomViewServiceImpl implements RoomViewService {

  private final RoomService roomService;
  private final TemperatureValueDao temperatureValueDao;
  private final EnergyMetricDao energyMetricDao;
  private final HealthCheckService healthCheckService;
  private final EngineProperties engineProperties;

  @Override
  public RoomDetailViewModel getRoomDetailModel(RoomDetailCriteria req) {
    if (req == null || req.getRoomId() == null) {
      throw new IllegalArgumentException("Room ID must be provided");
    }

    Instant endedAt = Instant.now();
    Instant startedAt = endedAt.minusSeconds(engineProperties.getRoomStatusLookbackSeconds());
    var room = roomService.getById(req.getRoomId());

    Double lastestAvgTemperature = null;
    try {
      lastestAvgTemperature = temperatureValueDao.getAverageHistoryByRoom(req.getRoomId(), startedAt, endedAt,
          TelemetryTimeGroup.getDivisorForRange(startedAt, endedAt)).getLast().avgTempC();
    } catch (Exception e) {
    }

    Double latestSumWatt = null;
    try {
      latestSumWatt = energyMetricDao.findHistory(EnergyMetricCategory.ROOM, req.getRoomId(), startedAt, endedAt,
          TelemetryTimeGroup.getDivisorForRange(startedAt, endedAt)).getLast().getPower();
    } catch (Exception e) {
    }

    int healthScore = healthCheckService.getHealthScoreByRoom(req.getRoomId());

    return RoomDetailViewModel.builder()
        .room(room)
        .lastestAvgTemperature(lastestAvgTemperature)
        .lastestSumWatt(latestSumWatt)
        .healthScore(healthScore)
        .build();
  }
}
