package com.iviet.ivshs.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.RoomDetailViewModel;
import com.iviet.ivshs.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.service.RoomDetailViewService;
import com.iviet.ivshs.util.LocalContextUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomDetailViewServiceImpl implements RoomDetailViewService {

  private final RoomDao roomDao;
  private final TemperatureValueDao temperatureValueDao;
  private final EnergyMetricDao energyMetricDao;

  @Override
  public RoomDetailViewModel getModel(GetModelCriteria req) {
    if (req == null || req.getRoomId() == null) {
      throw new IllegalArgumentException("Room ID must be provided");
    }

    Instant endedAt = Instant.now();
    Instant startedAt = endedAt.minusSeconds(5 * 60); // Last 5 minutes
    String langCode = LocalContextUtil.getCurrentLangCode();

    var room = roomDao.findById(req.getRoomId(), langCode).orElseThrow();

    Double lastestAvgTemperature = null;
    try {
      lastestAvgTemperature = temperatureValueDao.getAverageHistoryByRoom(req.getRoomId(), startedAt, endedAt, TelemetryTimeGroup.getDivisorForRange(startedAt, endedAt)).getLast().avgTempC();
    } catch (Exception e) {
    }

    Double latestSumWatt = null;
    try {
      latestSumWatt = energyMetricDao.findHistory(EnergyMetricCategory.ROOM, req.getRoomId(), startedAt, endedAt, TelemetryTimeGroup.getDivisorForRange(startedAt, endedAt)).getLast().getPower();
    } catch (Exception e) {
    } 
    
    return RoomDetailViewModel.builder()
      .room(room)
      .lastestAvgTemperature(lastestAvgTemperature)
      .lastestSumWatt(latestSumWatt)
      .build();
  }
}
