package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.HealthCheckRequestDtoV1;
import com.iviet.ivshs.dto.HealthCheckResponseDtoV1;

public interface HealthCheckServiceV1 {
	HealthCheckResponseDtoV1 check(HealthCheckRequestDtoV1 request);
}
