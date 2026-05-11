package com.iviet.ivshs.service.impl;

import java.time.Instant;
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
import com.iviet.ivshs.service.IndexViewService;
import com.iviet.ivshs.util.LocalContextUtil;

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
    Instant startedAt = endedAt.minusSeconds(5 * 60); // Last 5 minutes
    String langCode = LocalContextUtil.getCurrentLangCode();
    var floors = floorDao.findAll(langCode);
    var rooms = roomDao.findAll(langCode);
    var floorRoomMap = floors.stream()
      .collect(Collectors.toMap(
        floor -> floor,
        floor -> rooms.stream().filter(room -> room.floorId().equals(floor.id())).toList()
    ));

    Map<Long, IndexViewModel.RoomInfo> roomInfoMap = rooms.stream()
      .collect(Collectors.toMap(
        room -> room.id(),
        room -> {
          Long hardwareCount = hardwareConfigDao.countByRoomId(room.id());
          Double lastestAvgTemperature = temperatureValueDao.getAverageHistoryByRoom(room.id(), startedAt, endedAt, 0).stream().findFirst().map(t -> t.avgTempC()).orElse(null);
          Double latestSumWatt = energyMetricDao.findHistory(EnergyMetricCategory.ROOM, room.id(), startedAt, endedAt, 0).stream().findFirst().map(t -> t.getPower()).orElse(null);
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

    return new IndexViewModel(floorRoomMap, roomInfoMap, totalFloors, totalRooms, totalHardwares);
  }
  
}
