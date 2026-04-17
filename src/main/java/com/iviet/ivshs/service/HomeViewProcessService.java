package com.iviet.ivshs.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.iviet.ivshs.dto.FloorDto;
import com.iviet.ivshs.dto.RoomDto;

public interface HomeViewProcessService {
	
	Map<Long, List<RoomDto>> getFloorRoomsMap();
	
	Map<Long, FloorDto> getFloorsMap();

	Long getDeviceCountByRoom(Long roomId);

	Map<Long, Long> getDeviceCountMap(List<Long> roomIds);

	Optional<Double> getLatestTemperatureForRoom(Long roomId);

	Optional<Double> getLatestPowerConsumptionForRoom(Long roomId);
}
