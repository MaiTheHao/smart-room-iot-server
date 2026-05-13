package com.iviet.ivshs.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.FloorDao;
import com.iviet.ivshs.dao.HardwareConfigDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.IndexViewModel;
import com.iviet.ivshs.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.service.IndexViewService;
import com.iviet.ivshs.util.LocalContextUtil;
import com.iviet.ivshs.util.TimeUtil;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IndexViewServiceImpl implements IndexViewService {

  private final FloorDao floorDao;
  private final RoomDao roomDao;
  private final HardwareConfigDao hardwareConfigDao;
  private final TemperatureValueDao temperatureValueDao;
  private final EnergyMetricDao energyMetricDao;

  @Override
  public IndexViewModel getModel() {
    Instant endedAt = Instant.now();
    Instant startedAt = endedAt.minusSeconds(5 * 60);
    String langCode = LocalContextUtil.getCurrentLangCode();

    var floors = floorDao.findAll(langCode);
    var rooms = roomDao.findAll(langCode);
    var floorRoomMap = floors.stream()
      .collect(Collectors.toMap(
        floor -> floor.id(),
        floor -> rooms.stream().filter(room -> room.floorId().equals(floor.id())).toList()
    ));

    Map<Long, IndexViewModel.RoomInfo> roomInfoMap = rooms.stream()
      .collect(Collectors.toMap(
        room -> room.id(),
        room -> {
          Long hardwareCount = hardwareConfigDao.countByRoomId(room.id());
          int divisor = TelemetryTimeGroup.getDivisorForRange(startedAt, endedAt);

          var tempHistory = temperatureValueDao.getAverageHistoryByRoom(room.id(), startedAt, endedAt, divisor);
          Double lastestAvgTemperature = (tempHistory != null && !tempHistory.isEmpty())
              ? tempHistory.get(tempHistory.size() - 1).avgTempC()
              : null;

          var energyHistory = energyMetricDao.findHistory(EnergyMetricCategory.ROOM, room.id(), startedAt, endedAt, divisor);
          Double latestSumWatt = (energyHistory != null && !energyHistory.isEmpty())
              ? energyHistory.get(energyHistory.size() - 1).getPower()
              : null;

          return IndexViewModel.RoomInfo.builder()
            .hardwareCount(hardwareCount)
            .latestAvgTemperature(lastestAvgTemperature)
            .latestSumWatt(latestSumWatt)
            .build();
        }
    ));

    Long totalFloors = floorDao.count();
    Long totalRooms = roomDao.count();
    Long totalHardwares = hardwareConfigDao.count();

    return new IndexViewModel(
      floors != null ? floors : List.of(),
      floorRoomMap != null ? floorRoomMap : Map.of(),
      roomInfoMap != null ? roomInfoMap : Map.of(),
      totalFloors,
      totalRooms,
      totalHardwares
    );
  }
  
}