package com.iviet.ivshs.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.DeviceCategoryV1;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchTelemetryByGatewayResponseDtoV1 {
	private int status;
	private String message;
	private Instant timestamp;
	private List<Data> data;

	@Getter
	@Builder
	public static class Data {
		private String naturalId;
		private DeviceCategoryV1 category;
		private boolean isActive;
		private JsonNode data;
	}
}	
