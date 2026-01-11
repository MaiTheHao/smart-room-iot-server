package com.iviet.ivshs.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;

import lombok.Builder;

@Builder
public record RoomDetailViewModel(
	RoomDto room,
	String pageTitle,
	Optional<Double> currentTemp,
	Optional<Double> currentPower,
	List<?> tempChartData,
	List<?> powerChartData,
	List<LightDto> lights,
	String errorMessage
) {
	public Map<String, Object> toModelAttributes() {
		return Map.of(
			"room", room,
			"pageTitle", pageTitle != null ? pageTitle : "",
			"currentTemp", currentTemp != null ? currentTemp : Optional.empty(),
			"currentPower", currentPower != null ? currentPower : Optional.empty(),
			"tempChartData", tempChartData != null ? tempChartData : Collections.emptyList(),
			"powerChartData", powerChartData != null ? powerChartData : Collections.emptyList(),
			"lights", lights != null ? lights : Collections.emptyList(),
			"errorMessage", errorMessage != null ? errorMessage : ""
		);
	}
}
