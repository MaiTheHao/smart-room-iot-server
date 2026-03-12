package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.iviet.ivshs.constant.I18nMessageConstant;
import com.iviet.ivshs.dto.FloorDto;
import com.iviet.ivshs.dto.HomeViewModel;
import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.service.HomeViewProcessService;
import com.iviet.ivshs.service.HomeViewService;
import com.iviet.ivshs.service.I18nMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeViewServiceImpl implements HomeViewService {

	private final HomeViewProcessService cacheService;
	private final I18nMessageService i18nMessageService;

	@Override
	public HomeViewModel getModel() {
		Map<Long, FloorDto> floorsMap = cacheService.getFloorsMap();
		Map<Long, List<RoomDto>> floorRoomsMap = cacheService.getFloorRoomsMap();

		List<Long> allRoomIds = floorRoomsMap.values().stream()
			.flatMap(List::stream)
			.map(RoomDto::id)
			.toList();

		return HomeViewModel.builder()
			.welcomeMessage(i18nMessageService.getMessage(I18nMessageConstant.WELCOME_MSG))
			.floorsMap(floorsMap)
			.floorRoomsMap(floorRoomsMap)
			.roomGatewayCountMap(getGatewayCountsForRooms(allRoomIds))
			.roomLatestAvgTempMap(getLatestTemperaturesForRooms(allRoomIds))
			.roomLatestSumWattMap(getLatestPowerConsumptionsForRooms(allRoomIds))
			.build();
	}
	
	private Map<Long, Long> getGatewayCountsForRooms(List<Long> roomIds) {
		return roomIds.stream().collect(Collectors.toMap(
			id -> id,
			cacheService::getDeviceCountByRoom
		));
	}

	private Map<Long, Optional<Double>> getLatestTemperaturesForRooms(List<Long> roomIds) {
		return roomIds.stream().collect(Collectors.toMap(
			id -> id,
			cacheService::getLatestTemperatureForRoom
		));
	}

	private Map<Long, Optional<Double>> getLatestPowerConsumptionsForRooms(List<Long> roomIds) {
		return roomIds.stream().collect(Collectors.toMap(
			id -> id,
			cacheService::getLatestPowerConsumptionForRoom
		));
	}
}