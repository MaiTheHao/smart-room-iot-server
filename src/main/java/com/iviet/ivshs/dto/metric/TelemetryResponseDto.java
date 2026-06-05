package com.iviet.ivshs.dto.metric;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelemetryResponseDto {
	private int status;
	private String message;
	private Instant timestamp;
	private RoomData data;

	@Getter
	@Builder
	@Jacksonized
	public static class RoomData {
		private String roomCode;
		private List<DeviceDto> devices;
	}

	@Getter
	@Builder
	@Jacksonized
	public static class DeviceDto {
		private String naturalId;
		private DeviceCategory category;
		private JsonNode data;
	}
}