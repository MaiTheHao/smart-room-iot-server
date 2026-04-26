package com.iviet.ivshs.controller.api.v1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.service.SetupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "SETUP-CTRL")
@RestController
@RequestMapping("/api/v1/setup")
@RequiredArgsConstructor
public class SetupController {
	
	private final SetupService setupService;
	
	@PostMapping("/{clientId}")
	public ResponseEntity<ApiResponse<?>> setup(@PathVariable(name = "clientId") Long clientId) {
		log.info("Start: clientId={}", clientId);
		setupService.setup(clientId);
		
		log.info("Success: clientId={}", clientId);
		
		return ResponseEntity.ok(ApiResponse.builder()
			.status(HttpStatus.OK.value())
			.message("Device setup completed successfully")
			.data(null)
			.timestamp(java.time.Instant.now())
			.build());
	}
}