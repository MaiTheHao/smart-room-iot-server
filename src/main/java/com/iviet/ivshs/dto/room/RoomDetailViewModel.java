package com.iviet.ivshs.dto.room;

import java.util.Map;

import org.springframework.lang.NonNull;

import java.util.HashMap;

import lombok.Builder;

@Builder
public record RoomDetailViewModel(
	RoomDto room,
	Double lastestAvgTemperature,
	Double lastestSumWatt,
	Integer healthScore
) {

	@NonNull
	public Map<String, Object> toModelAttributes() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("room", room);
		attributes.put("lastestAvgTemperature", lastestAvgTemperature);
		attributes.put("lastestSumWatt", lastestSumWatt);
		attributes.put("healthScore", healthScore);
		return attributes;
	}
}
