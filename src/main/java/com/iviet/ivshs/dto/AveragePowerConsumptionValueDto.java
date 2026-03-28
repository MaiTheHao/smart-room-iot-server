package com.iviet.ivshs.dto;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import com.iviet.ivshs.util.TimeUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AveragePowerConsumptionValueDto {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	
	private Instant timestamp;
	private Double avgWatt;

	public AveragePowerConsumptionValueDto(String formattedTimestamp, Double avgWatt) {
		this.timestamp = TimeUtil.parseToInstant(formattedTimestamp, FORMATTER);
		this.avgWatt = avgWatt;
	}
}
