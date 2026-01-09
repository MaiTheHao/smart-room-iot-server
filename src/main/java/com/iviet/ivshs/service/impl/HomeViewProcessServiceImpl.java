package com.iviet.ivshs.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.FloorDto;
import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.enumeration.CacheDefinition;
import com.iviet.ivshs.service.DeviceControlService;
import com.iviet.ivshs.service.FloorService;
import com.iviet.ivshs.service.HomeViewProcessService;
import com.iviet.ivshs.service.PowerConsumptionValueService;
import com.iviet.ivshs.service.RoomService;
import com.iviet.ivshs.service.TemperatureValueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeViewProcessServiceImpl implements HomeViewProcessService {
	
	private final FloorService floorService;
	private final RoomService roomService;
	private final DeviceControlService deviceControlService;
	private final TemperatureValueService temperatureValueService;
	private final PowerConsumptionValueService powerConsumptionValueService;
	private static final long DEFAULT_MINUS_MINUTES = 15;
	
	@Override
	@Cacheable(
		value = CacheDefinition._HOME_VIEW_FLOOR_ROOMS_MAP,
		key = "'all' + T(com.iviet.ivshs.util.LocalContextUtil).getCurrentLangCodeFromRequest() + T(com.iviet.ivshs.util.SecurityContextUtil).getCurrentUsername()"
	)
	public Map<Long, List<RoomDto>> getFloorRoomsMap() {
		List<FloorDto> floors = floorService.getList(0, 1000).content();
		
		return floors.stream()
			.collect(Collectors.toMap(
				FloorDto::id,                   
				floor -> roomService.getListByFloor(floor.id(), 0, 1000).content(),
				(oldValue, newValue) -> oldValue,
				LinkedHashMap::new
			));
	}
	
	@Override
	@Cacheable(
		value = CacheDefinition._HOME_VIEW_FLOORS_MAP,
		key = "'all' + T(com.iviet.ivshs.util.LocalContextUtil).getCurrentLangCodeFromRequest() + T(com.iviet.ivshs.util.SecurityContextUtil).getCurrentUsername()"
	)
	public Map<Long, FloorDto> getFloorsMap() {
		List<FloorDto> floors = floorService.getList(0, 1000).content();
		
		return floors.stream()
			.collect(Collectors.toMap(
				FloorDto::id,
				floor -> floor,
				(oldValue, newValue) -> oldValue,
				LinkedHashMap::new
			));
	}
	

	@Override
	@Cacheable(
		value = CacheDefinition._HOME_VIEW_ROOM_GATEWAY_COUNT,
		key = "#a0 + T(com.iviet.ivshs.util.LocalContextUtil).getCurrentLangCodeFromRequest() + T(com.iviet.ivshs.util.SecurityContextUtil).getCurrentUsername()"
	)
	public Long getGatewayCountForRoom(Long roomId) {
		return deviceControlService.countByRoomId(roomId);
	}
	
	@Override
	@Cacheable(
		value = CacheDefinition._HOME_VIEW_ROOM_LASTEST_TEMP,
		key = "#a0 + T(com.iviet.ivshs.util.LocalContextUtil).getCurrentLangCodeFromRequest() + T(com.iviet.ivshs.util.SecurityContextUtil).getCurrentUsername()"
	)
	public Double getLatestTemperatureForRoom(Long roomId) {
		Instant endedAt = Instant.now();
		Instant startedAt = endedAt.minus(DEFAULT_MINUS_MINUTES, ChronoUnit.MINUTES);
		
		var temperatureHistory = temperatureValueService
			.getAverageTemperatureByRoom(roomId, startedAt, endedAt);
		
		return temperatureHistory.isEmpty() 
			? 0.0 
			: temperatureHistory.getLast().avgTempC();
	}
	
	@Override
	@Cacheable(
		value = CacheDefinition._HOME_VIEW_ROOM_LASTEST_POWER,
		key = "#a0 + T(com.iviet.ivshs.util.LocalContextUtil).getCurrentLangCodeFromRequest()"
	)
	public Double getLatestPowerConsumptionForRoom(Long roomId) {
		Instant endedAt = Instant.now();
		Instant startedAt = endedAt.minus(DEFAULT_MINUS_MINUTES, ChronoUnit.MINUTES);
		
		var powerHistory = powerConsumptionValueService
			.getSumPowerConsumptionByRoom(roomId, startedAt, endedAt);
		
		return powerHistory.isEmpty() 
			? 0.0 
			: powerHistory.getLast().getSumWatt();
	}

	@Override
	@CacheEvict(allEntries = true, value = {
		CacheDefinition._HOME_VIEW_FLOOR_ROOMS_MAP,
		CacheDefinition._HOME_VIEW_FLOORS_MAP,
		CacheDefinition._HOME_VIEW_ROOM_GATEWAY_COUNT,
		CacheDefinition._HOME_VIEW_ROOM_LASTEST_TEMP,
		CacheDefinition._HOME_VIEW_ROOM_LASTEST_POWER
	})
	public void evictAllCaches() {}
}
