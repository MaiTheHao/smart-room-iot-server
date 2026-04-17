package com.iviet.ivshs.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AveragePowerConsumptionValueDto {
	private Instant timestamp;
	private Double avgWatt;

	public AveragePowerConsumptionValueDto(Long unixSeconds, Double avgWatt) {
		this.timestamp = Instant.ofEpochSecond(unixSeconds);
		this.avgWatt = avgWatt;
	}
}
