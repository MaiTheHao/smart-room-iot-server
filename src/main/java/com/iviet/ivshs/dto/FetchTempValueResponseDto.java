package com.iviet.ivshs.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchTempValueResponseDto {
	private int status;
	private String message;
	private Instant timestamp;
	private Data data;

	@Getter
	@Builder
	public static class Data {
		private double tempC;
	}
}