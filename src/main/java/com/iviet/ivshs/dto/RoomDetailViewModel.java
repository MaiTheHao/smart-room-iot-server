package com.iviet.ivshs.dto;

import java.util.Map;

import org.springframework.lang.NonNull;

import java.util.HashMap;

import lombok.Builder;

@Builder
public record RoomDetailViewModel(RoomDto room, Double latestAvgTemperature, Double latestSumWatt, Integer healthScore) {

	@NonNull
	public Map<String, Object> toModelAttributes() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("room", room);
		attributes.put("latestAvgTemperature", latestAvgTemperature);
		attributes.put("latestSumWatt", latestSumWatt);
		attributes.put("healthScore", healthScore);
		return attributes;
	}
}
