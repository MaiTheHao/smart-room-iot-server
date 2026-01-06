package com.iviet.ivshs.controller.api.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/telemetries")
@RequiredArgsConstructor
public class TelemetryController {

	private final TelemetryService telemetryService;

	@PostMapping("/gateway/{gatewayUsername}")
	public ResponseEntity<ApiResponse<?>> fetchByGateway(@PathVariable(name = "gatewayUsername") String gatewayUsername) {
		log.info("Fetching telemetry for gateway: {}", gatewayUsername);
		telemetryService.takeByGateway(gatewayUsername);
		return ResponseEntity.ok(ApiResponse.ok(null));
	}

	@PostMapping("/room/{roomCode}")
	public ResponseEntity<ApiResponse<?>> fetchByRoom(@PathVariable(name = "roomCode") String roomCode) {
		log.info("Fetching telemetry for room: {}", roomCode);
		telemetryService.takeByRoom(roomCode);
		return ResponseEntity.ok(ApiResponse.ok(null));
	}

	@PostMapping("/temperature/{naturalId}")
	public ResponseEntity<ApiResponse<?>> fetchTemperature(@PathVariable(name = "naturalId") String naturalId) {
		log.info("Fetching temperature data for sensor: {}", naturalId);
		telemetryService.takeTemperatureData(naturalId);
		return ResponseEntity.ok(ApiResponse.ok(null));
	}

	@PostMapping("/power-consumption/{naturalId}")
	public ResponseEntity<ApiResponse<?>> fetchPowerConsumption(@PathVariable(name = "naturalId") String naturalId) {
		log.info("Fetching power consumption data for sensor: {}", naturalId);
		telemetryService.takePowerConsumptionData(naturalId);
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
}
