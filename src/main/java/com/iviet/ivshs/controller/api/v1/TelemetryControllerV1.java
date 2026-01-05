package com.iviet.ivshs.controller.api.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.iviet.ivshs.dto.ApiResponseV1;
import com.iviet.ivshs.service.TelemetryServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/telemetries")
@RequiredArgsConstructor
public class TelemetryControllerV1 {

	private final TelemetryServiceV1 telemetryService;

	@PostMapping("/gateway/{gatewayUsername}")
	public ResponseEntity<ApiResponseV1<?>> fetchByGateway(@PathVariable String gatewayUsername) {
		log.info("Fetching telemetry for gateway: {}", gatewayUsername);
		telemetryService.takeByGateway(gatewayUsername);
		return ResponseEntity.ok(ApiResponseV1.ok(null));
	}

	@PostMapping("/room/{roomCode}")
	public ResponseEntity<ApiResponseV1<?>> fetchByRoom(@PathVariable String roomCode) {
		log.info("Fetching telemetry for room: {}", roomCode);
		telemetryService.takeByRoom(roomCode);
		return ResponseEntity.ok(ApiResponseV1.ok(null));
	}

	@PostMapping("/temperature/{naturalId}")
	public ResponseEntity<ApiResponseV1<?>> fetchTemperature(@PathVariable String naturalId) {
		log.info("Fetching temperature data for sensor: {}", naturalId);
		telemetryService.takeTemperatureData(naturalId);
		return ResponseEntity.ok(ApiResponseV1.ok(null));
	}

	@PostMapping("/power-consumption/{naturalId}")
	public ResponseEntity<ApiResponseV1<?>> fetchPowerConsumption(@PathVariable String naturalId) {
		log.info("Fetching power consumption data for sensor: {}", naturalId);
		telemetryService.takePowerConsumptionData(naturalId);
		return ResponseEntity.ok(ApiResponseV1.ok(null));
	}
}
