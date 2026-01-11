package com.iviet.ivshs.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Builder;

@Builder
public record HomeViewModel (
	String welcomeMessage,
	Map<Long, FloorDto> floorsMap,             
	Map<Long, List<RoomDto>> floorRoomsMap,     
	Map<Long, Long> roomGatewayCountMap,        
	Map<Long, Optional<Double>> roomLatestAvgTempMap,     
	Map<Long, Optional<Double>> roomLatestSumWattMap      
) {
	public Map<String, Object> toModelAttributes() {
		return Map.of(
			"welcomeMessage", welcomeMessage,
			"floorsMap", floorsMap,
			"floorRoomsMap", floorRoomsMap,
			"roomGatewayCountMap", roomGatewayCountMap,
			"roomLastestAvgTempMap", roomLatestAvgTempMap,
			"roomLastestSumWattMap", roomLatestSumWattMap
		);
	}
}
