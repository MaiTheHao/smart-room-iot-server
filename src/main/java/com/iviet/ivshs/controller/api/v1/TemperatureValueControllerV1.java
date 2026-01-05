package com.iviet.ivshs.controller.api.v1;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.AverageTemperatureValueDtoV1;
import com.iviet.ivshs.service.TemperatureValueServiceV1;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TemperatureValueControllerV1 {

	private final TemperatureValueServiceV1 temperatureValueService;

	@GetMapping("/room/{roomId}/temperature-values/average")
	public ResponseEntity<List<AverageTemperatureValueDtoV1>> getAverageByRoom(
			@PathVariable(name = "roomId") Long roomId,
			@RequestParam(name = "from") Instant from,
			@RequestParam(name = "to") Instant to) {
		return ResponseEntity.ok(temperatureValueService.getAverageTemperatureByRoom(roomId, from, to));
	}
}
