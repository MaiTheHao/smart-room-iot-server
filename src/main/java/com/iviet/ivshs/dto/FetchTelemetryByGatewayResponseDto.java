package com.iviet.ivshs.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.DeviceCategory;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchTelemetryByGatewayResponseDto {
	private int status;
	private String message;
	private Instant timestamp;
	private List<Data> data;

	@Getter
	@Builder
	@Jacksonized
	public static class Data {
		private String naturalId;
		private DeviceCategory category;
		private boolean isActive;
		private JsonNode data;
	}
}	
