package com.iviet.ivshs.controller.api.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.service.telemetry.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/telemetries")
@RequiredArgsConstructor
public class TelemetryController {

	private final TelemetryService telemetryService;

	@PostMapping("/gateway/{gatewayUsername}")
	public ResponseEntity<ApiResponse<?>> fetchByGateway(@PathVariable(name = "gatewayUsername")
	String gatewayUsername) {
		log.info("Fetching telemetry: gateway={}", gatewayUsername);
		telemetryService.takeByGateway(gatewayUsername);
		return ResponseEntity.ok(ApiResponse.ok(null));
	}

	@PostMapping("/room/{roomCode}")
	public ResponseEntity<ApiResponse<?>> fetchByRoom(@PathVariable(name = "roomCode")
	String roomCode) {
		log.info("Fetching telemetry: room={}", roomCode);
		telemetryService.takeByRoom(roomCode);
		return ResponseEntity.ok(ApiResponse.ok(null));
	}

}
