package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dto.FloorDto;
import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeViewProcessServiceImpl implements HomeViewProcessService {

	private final FloorService floorService;
	private final RoomService roomService;
	private final DeviceMetadataService deviceMetadataService;
	private final TemperatureValueService temperatureValueService;
	private final PowerConsumptionValueService powerConsumptionValueService;

	private static final long DEFAULT_MINUS_MINUTES = 15;

	@Override
	public Map<Long, FloorDto> getFloorsMap() {
		List<FloorDto> floors = floorService.getList(0, 1000).content();
		return floors.stream()
				.collect(Collectors.toMap(
						FloorDto::id,
						Function.identity(),
						(oldValue, newValue) -> oldValue,
						LinkedHashMap::new
				));
	}

	@Override
	public Map<Long, List<RoomDto>> getFloorRoomsMap() {
		List<RoomDto> allRooms = roomService.getAll();
		return allRooms.stream()
				.collect(Collectors.groupingBy(
						RoomDto::floorId,
						LinkedHashMap::new,
						Collectors.toList()
				));
	}

	@Override
	public Long getDeviceCountByRoom(Long roomId) {
		return deviceMetadataService.getCountByRoomId(roomId);
	}

	@Override
	public Map<Long, Long> getDeviceCountMap(List<Long> roomIds) {
		if (roomIds == null || roomIds.isEmpty()) return Map.of();
		
		var counts = roomService.getDeviceCountsByRoomIds(roomIds);
		return counts.stream().collect(Collectors.toMap(
			com.iviet.ivshs.dto.RoomDeviceCountDto::roomId,
			dto -> dto.lightCount() + dto.acCount() + dto.fanCount()
		));
	}

	@Override
	public Optional<Double> getLatestTemperatureForRoom(Long roomId) {
		TimeRange range = calculateDefaultTimeRange();
		var history = temperatureValueService.getAverageTemperatureByRoom(roomId, range.start(), range.end());

		return history.isEmpty() 
				? Optional.empty() 
				: Optional.of(history.getLast().avgTempC());
	}

	@Override
	public Optional<Double> getLatestPowerConsumptionForRoom(Long roomId) {
		TimeRange range = calculateDefaultTimeRange();
		var history = powerConsumptionValueService.getSumPowerConsumptionByRoom(roomId, range.start(), range.end());

		return history.isEmpty() 
				? Optional.empty() 
				: Optional.of(history.getLast().getSumWatt());
	}

	private TimeRange calculateDefaultTimeRange() {
		Instant now = Instant.now();
		return new TimeRange(now.minus(DEFAULT_MINUS_MINUTES, ChronoUnit.MINUTES), now);
	}

	private record TimeRange(Instant start, Instant end) {}
}
