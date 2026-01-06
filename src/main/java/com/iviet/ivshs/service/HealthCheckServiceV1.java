package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.HealthCheckResponseDto;
import java.util.Map;

public interface HealthCheckServiceV1 {
    HealthCheckResponseDto checkByClient(String ipAddress);
    HealthCheckResponseDto checkByClient(Long clientId);

    Map<String, HealthCheckResponseDto> checkByRoom(String roomCode);
    Map<String, HealthCheckResponseDto> checkByRoom(Long roomId);

    int getHealthScoreByClient(Long clientId);
    int getHealthScoreByRoom(Long roomId);
}