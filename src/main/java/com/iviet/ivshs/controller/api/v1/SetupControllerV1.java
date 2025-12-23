package com.iviet.ivshs.controller.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iviet.ivshs.dto.ApiResponseV1;
import com.iviet.ivshs.dto.SetupRequestV1;
import com.iviet.ivshs.dto.SetupResponseV1;
import com.iviet.ivshs.service.SetupServiceV1;

@RestController
@RequestMapping("/api/v1/setup")
public class SetupControllerV1 {
	
	@Autowired
	private SetupServiceV1 setupService;
	
	@PostMapping
	public ResponseEntity<ApiResponseV1<SetupResponseV1>> setup(@RequestBody SetupRequestV1 req) {
		SetupResponseV1 response = setupService.setup(req);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponseV1.created(response));
	}
}