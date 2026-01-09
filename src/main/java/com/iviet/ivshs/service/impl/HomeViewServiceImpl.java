package com.iviet.ivshs.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		Map<Long, Long> gatewayCountMap = getGatewayCountsForRooms(allRoomIds);
		Map<Long, Double> avgTempMap = getLatestTemperaturesForRooms(allRoomIds);
		Map<Long, Double> sumWattMap = getLatestPowerConsumptionsForRooms(allRoomIds);

		return HomeViewModel.builder()
			.welcomeMessage(i18nMessageService.getMessage(I18nMessageConstant.WELCOME_MSG))
			.floorsMap(floorsMap)
			.floorRoomsMap(floorRoomsMap)
			.roomGatewayCountMap(gatewayCountMap)
			.roomLatestAvgTempMap(avgTempMap)
			.roomLatestSumWattMap(sumWattMap)
			.build();
	}

	public void refreshDashboardData() {
		cacheService.evictAllCaches();
	}
	
	private Map<Long, Long> getGatewayCountsForRooms(List<Long> roomIds) {
		Map<Long, Long> result = new HashMap<>();
		for (Long roomId : roomIds) {
			result.put(roomId, cacheService.getGatewayCountForRoom(roomId));
		}
		return result;
	}
	
	private Map<Long, Double> getLatestTemperaturesForRooms(List<Long> roomIds) {
		Map<Long, Double> result = new HashMap<>();
		for (Long roomId : roomIds) {
			result.put(roomId, cacheService.getLatestTemperatureForRoom(roomId));
		}
		return result;
	}
	
	private Map<Long, Double> getLatestPowerConsumptionsForRooms(List<Long> roomIds) {
		Map<Long, Double> result = new HashMap<>();
		for (Long roomId : roomIds) {
			result.put(roomId, cacheService.getLatestPowerConsumptionForRoom(roomId));
		}
		return result;
	}
}

