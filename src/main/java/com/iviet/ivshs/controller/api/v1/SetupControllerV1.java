package com.iviet.ivshs.controller.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.ApiResponseV1;
import com.iviet.ivshs.service.SetupServiceV1;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/setup")
public class SetupControllerV1 {
	
	@Autowired
	private SetupServiceV1 setupService;
	
	@PostMapping("/{clientId}")
	public ResponseEntity<ApiResponseV1<?>> setup(@PathVariable(name = "clientId") Long clientId) {
		log.info("[SETUP_CTRL_START] Starting setup for clientId={}", clientId);
		setupService.setup(clientId);
		
		log.info("[SETUP_CTRL_SUCCESS] Setup completed for clientId={}", clientId);
		
		return ResponseEntity.ok(ApiResponseV1.builder()
			.status(HttpStatus.OK.value())
			.message("Device setup completed successfully")
			.data(null)
			.timestamp(java.time.Instant.now())
			.build());
	}
}