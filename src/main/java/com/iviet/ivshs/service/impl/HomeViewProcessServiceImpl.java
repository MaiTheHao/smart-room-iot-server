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
		return floorService.getList(0, 1000).content().stream()
				.collect(Collectors.toMap(
						FloorDto::id,
						Function.identity(),
						(oldValue, newValue) -> oldValue,
						LinkedHashMap::new
				));
	}

	@Override
	public Map<Long, List<RoomDto>> getFloorRoomsMap() {
		return floorService.getList(0, 1000).content().stream()
				.collect(Collectors.toMap(
						FloorDto::id,
						floor -> roomService.getListByFloor(floor.id(), 0, 1000).content(),
						(oldValue, newValue) -> oldValue,
						LinkedHashMap::new
				));
	}

	@Override
	public Long getDeviceCountByRoom(Long roomId) {
		return deviceMetadataService.getCountByRoomId(roomId);
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

	@Override
	public void evictAllCaches() {
		log.debug("[HOME-VIEW] Cache eviction triggered");
	}

	private TimeRange calculateDefaultTimeRange() {
		Instant now = Instant.now();
		return new TimeRange(now.minus(DEFAULT_MINUS_MINUTES, ChronoUnit.MINUTES), now);
	}

	private record TimeRange(Instant start, Instant end) {}
}
