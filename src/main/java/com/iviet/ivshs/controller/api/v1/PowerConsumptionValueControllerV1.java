package com.iviet.ivshs.controller.api.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.SumPowerConsumptionValueDto;
import com.iviet.ivshs.service.PowerConsumptionValueServiceV1;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PowerConsumptionValueControllerV1 {

	private final PowerConsumptionValueServiceV1 powerConsumptionValueService;

	@GetMapping("/rooms/{roomId}/power-consumptions/sum-history")
	public ResponseEntity<List<SumPowerConsumptionValueDto>> oldGetSumByRoom(
			@PathVariable(name = "roomId") Long roomId,
			@RequestParam(name = "startedAt") Instant from,
			@RequestParam(name = "endedAt") Instant to) {
		return ResponseEntity.ok(powerConsumptionValueService.getSumPowerConsumptionByRoom(roomId, from, to));
	}

	@GetMapping("/rooms/{roomId}/power-consumption-values/sum")
	public ResponseEntity<List<SumPowerConsumptionValueDto>> getSumByRoom(
			@PathVariable(name = "roomId") Long roomId,
			@RequestParam(name = "from") Instant from,
			@RequestParam(name = "to") Instant to) {
		return ResponseEntity.ok(powerConsumptionValueService.getSumPowerConsumptionByRoom(roomId, from, to));
	}
}
