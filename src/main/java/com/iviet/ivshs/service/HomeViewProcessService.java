package com.iviet.ivshs.service;

import java.util.List;
import java.util.Map;

import com.iviet.ivshs.dto.FloorDto;
import com.iviet.ivshs.dto.RoomDto;

public interface HomeViewProcessService {
	
	Map<Long, List<RoomDto>> getFloorRoomsMap();
	
	Map<Long, FloorDto> getFloorsMap();

	Long getGatewayCountForRoom(Long roomId);

	Double getLatestTemperatureForRoom(Long roomId);

	Double getLatestPowerConsumptionForRoom(Long roomId);

	void evictAllCaches();
}
