package com.iviet.ivshs.controller.api.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.SumPowerConsumptionValueDtoV1;
import com.iviet.ivshs.service.PowerConsumptionValueServiceV1;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PowerConsumptionValueControllerV1 {

	private final PowerConsumptionValueServiceV1 powerConsumptionValueService;

	@GetMapping("/room/{roomId}/power-consumption-values/sum")
	public ResponseEntity<List<SumPowerConsumptionValueDtoV1>> getSumByRoom(
			@PathVariable(name = "roomId") Long roomId,
			@RequestParam(name = "from") Instant from,
			@RequestParam(name = "to") Instant to) {
		return ResponseEntity.ok(powerConsumptionValueService.getSumPowerConsumptionByRoom(roomId, from, to));
	}
}
