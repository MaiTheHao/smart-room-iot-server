package com.iviet.ivshs.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchTempValueResponseDto {
	private int status;
	private String message;
	private Instant timestamp;
	private Data data;

	@Getter
	@Builder
	@Jacksonized
	public static class Data {
		private double tempC;
	}
}