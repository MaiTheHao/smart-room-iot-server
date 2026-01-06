package com.iviet.ivshs.controller.api.v1;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.service.TemperatureValueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TemperatureValueController {

	private final TemperatureValueService temperatureValueService;
	
	@GetMapping("rooms/{roomId}/temperatures/average-history")
	public ResponseEntity<List<AverageTemperatureValueDto>> oldGetAverageByRoom(
			@PathVariable(name = "roomId") Long roomId,
			@RequestParam(name = "startedAt") Instant from,
			@RequestParam(name = "endedAt") Instant to) {
		return ResponseEntity.ok(temperatureValueService.getAverageTemperatureByRoom(roomId, from, to));
	}

	@GetMapping("/rooms/{roomId}/temperature-values/average")
	public ResponseEntity<List<AverageTemperatureValueDto>> getAverageByRoom(
			@PathVariable(name = "roomId") Long roomId,
			@RequestParam(name = "from") Instant from,
			@RequestParam(name = "to") Instant to) {
		return ResponseEntity.ok(temperatureValueService.getAverageTemperatureByRoom(roomId, from, to));
	}
}
