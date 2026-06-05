package com.iviet.ivshs.service.system.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.HardwareConfigDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.system.IndexViewModel;
import com.iviet.ivshs.shared.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.service.floor.FloorService;
import com.iviet.ivshs.service.room.RoomService;
import com.iviet.ivshs.service.system.IndexViewService;
import com.iviet.ivshs.shared.util.LocalContextUtil;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IndexViewServiceImpl implements IndexViewService {

	private final FloorService floorService;
	private final RoomService roomService;
	private final HardwareConfigDao hardwareConfigDao;
	private final TemperatureValueDao temperatureValueDao;
	private final EnergyMetricDao energyMetricDao;

	@Override
	public IndexViewModel getModel() {
		Instant endedAt = Instant.now();
		Instant startedAt = endedAt.minusSeconds(5 * 60);

		var floors = floorService.getAll();
		var rooms = roomService.getAll();

		Map<Long, List<com.iviet.ivshs.dto.room.RoomDto>> floorRoomMap = (floors == null) ? Map.of()
				: floors.stream()
						.collect(Collectors.toMap(
								floor -> floor.id(),
								floor -> (rooms == null) ? List.of()
										: rooms.stream()
												.filter(room -> room
														.floorId()
														.equals(floor.id()))
												.collect(Collectors
														.toList())));

		Map<Long, IndexViewModel.RoomInfo> roomInfoMap = (rooms == null) ? Map.of()
				: rooms.stream()
						.collect(Collectors.toMap(
								room -> room.id(),
								room -> {
									Long hardwareCount = hardwareConfigDao
											.countByRoomId(room.id());
									int divisor = TelemetryTimeGroup
											.getDivisorForRange(startedAt,
													endedAt);

									var tempHistory = temperatureValueDao
											.getAverageHistoryByRoom(
													room.id(),
													startedAt,
													endedAt,
													divisor);
									Double lastestAvgTemperature = (tempHistory != null
											&& !tempHistory.isEmpty())
													? tempHistory.get(
															tempHistory.size()
																	- 1)
															.avgTempC()
													: null;

									var energyHistory = energyMetricDao.findHistory(
											EnergyMetricCategory.ROOM,
											room.id(), startedAt,
											endedAt, divisor);
									Double latestSumWatt = (energyHistory != null
											&& !energyHistory.isEmpty())
													? energyHistory.get(
															energyHistory.size()
																	- 1)
															.getPower()
													: null;

									return IndexViewModel.RoomInfo.builder()
											.hardwareCount(hardwareCount)
											.latestAvgTemperature(
													lastestAvgTemperature)
											.latestSumWatt(latestSumWatt)
											.build();
								}));

		Long totalFloors = floors != null ? (long) floors.size() : 0L;
		Long totalRooms = rooms != null ? (long) rooms.size() : 0L;
		Long totalHardwares = (rooms == null) ? 0L
				: rooms.stream()
						.mapToLong(room -> hardwareConfigDao.countByRoomId(room.id()))
						.sum();

		return new IndexViewModel(
				floors != null ? floors : List.of(),
				floorRoomMap,
				roomInfoMap,
				totalFloors,
				totalRooms,
				totalHardwares);
	}

}
