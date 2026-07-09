package com.iviet.ivshs.service;

import java.util.Map;

import com.iviet.ivshs.dto.HealthCheckResponseDto;

public interface HealthCheckService {
    HealthCheckResponseDto checkByClient(String ipAddress);

    HealthCheckResponseDto checkByClient(Long clientId);

    Map<String, HealthCheckResponseDto> checkByRoom(String roomCode);

    Map<String, HealthCheckResponseDto> checkByRoom(Long roomId);

    int getHealthScoreByClient(Long clientId);

    int getHealthScoreByRoom(Long roomId);
}
