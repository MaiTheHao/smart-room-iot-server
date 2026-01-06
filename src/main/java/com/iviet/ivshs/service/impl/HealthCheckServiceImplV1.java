package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.HealthCheckResponseDto;
import com.iviet.ivshs.dto.HealthCheckResponseDto.DeviceDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.ExternalServiceException;
import com.iviet.ivshs.exception.domain.NetworkTimeoutException;
import com.iviet.ivshs.service.HealthCheckServiceV1;
import com.iviet.ivshs.util.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckServiceImplV1 implements HealthCheckServiceV1 {

    private final ClientDao clientDao;
    private final RoomDao roomDao;

    @Override
    public HealthCheckResponseDto checkByClient(Long clientId) {
        String ipAddress = clientDao.findGatewayById(clientId)
                .orElseThrow(() -> new BadRequestException("Client not found ID: " + clientId))
                .getIpAddress();
        return checkByClient(ipAddress);
    }

    @Override
    public HealthCheckResponseDto checkByClient(String ipAddress) {
        long start = System.currentTimeMillis();
        String url = UrlConstant.getHealthUrlV1(ipAddress);
        log.info("[HEALTH-CHECK] Starting health check for IP: {}", ipAddress);

        try {
            var response = HttpClientUtil.get(url);

            if (!response.isSuccess()) {
                log.warn("[HEALTH-CHECK] Failed IP [{}] with status {}. Body: {}", ipAddress, response.getStatusCode(), response.getBody());
                throw new ExternalServiceException("Health check failed with status " + response.getStatusCode());
            }

            HealthCheckResponseDto result = HttpClientUtil.fromJson(response.getBody(), HealthCheckResponseDto.class);
            log.info("[HEALTH-CHECK] Finished health check for IP: {} in {}ms", ipAddress, System.currentTimeMillis() - start);
            return result;
        } catch (NetworkTimeoutException | ExternalServiceException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("[HEALTH-CHECK] Response deserialization error for IP [{}]: {}", ipAddress, e.getMessage());
            throw new ExternalServiceException("Dữ liệu từ gateway không hợp lệ: " + ipAddress);
        } catch (Exception e) {
            log.error("[HEALTH-CHECK] Unexpected error for IP [{}]: {}", ipAddress, e.getMessage());
            throw new ExternalServiceException("Lỗi kết nối tới gateway: " + ipAddress);
        }
    }

    @Override
    public Map<String, HealthCheckResponseDto> checkByRoom(Long roomId) {
        String roomCode = roomDao.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Room not found ID: " + roomId))
                .getCode();
        return checkByRoom(roomCode);
    }

    @Override
    public Map<String, HealthCheckResponseDto> checkByRoom(String roomCode) {
        List<String> ipAddresses = clientDao.findGatewaysByRoomCode(roomCode).stream()
                .map(Client::getIpAddress)
                .toList();

        if (ipAddresses.isEmpty()) {
            throw new BadRequestException("No gateways found for Room: " + roomCode);
        }

        long start = System.currentTimeMillis();
        log.info("[HEALTH-CHECK] Starting batch health check for room [{}] - {} gateways", roomCode, ipAddresses.size());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Map.Entry<String, HealthCheckResponseDto>>> futures = ipAddresses.stream()
                    .map(ip -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return Map.entry(ip, checkByClient(ip));
                        } catch (Exception e) {
                            String domainMessage = mapExceptionToMessage(e);
                            return Map.entry(ip, HealthCheckResponseDto.builder()
                                    .status(500)
                                    .message(domainMessage)
                                    .timestamp(Instant.now().toString())
                                    .build());
                        }
                    }, executor))
                    .toList();

            Map<String, HealthCheckResponseDto> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            log.info("[HEALTH-CHECK] Finished batch health check for room [{}] in {}ms", roomCode, System.currentTimeMillis() - start);
            return results;
        }
    }

    private String mapExceptionToMessage(Exception e) {
        if (e instanceof NetworkTimeoutException) {
            return "Gateway timeout";
        }
        if (e instanceof ExternalServiceException) {
            return e.getMessage();
        }
        if (e instanceof IllegalArgumentException) {
            return "Invalid response from gateway";
        }
        return "Gateway communication failed";
    }

    @Override
    public int getHealthScoreByClient(Long clientId) {
        try {
            HealthCheckResponseDto result = checkByClient(clientId);
            return calculateScore(result);
        } catch (Exception e) {
            log.warn("[HEALTH-SCORE] Client [{}] calculation failed: {}", clientId, e.getMessage());
            return 0;
        }
    }

    @Override
    public int getHealthScoreByRoom(Long roomId) {
        Map<String, HealthCheckResponseDto> roomResults = checkByRoom(roomId);

        if (roomResults.isEmpty()) return 0;

        double averageScore = roomResults.values().stream()
                .mapToInt(this::calculateScore)
                .average()
                .orElse(0.0);

        return (int) Math.round(averageScore);
    }

    private int calculateScore(HealthCheckResponseDto dto) {
        if (dto == null) {
            log.debug("[HEALTH-SCORE] Response is null - gateway error");
            return 0;
        }

        if (dto.getStatus() != 200) {
            log.debug("[HEALTH-SCORE] Response status {} != 200: {}", dto.getStatus(), dto.getMessage());
            return 0;
        }

        if (dto.getData() == null) {
            log.debug("[HEALTH-SCORE] Response data is null - invalid response format");
            return 0;
        }

        List<DeviceDto> devices = dto.getData().getDevices();
        
        if (devices == null) {
            log.debug("[HEALTH-SCORE] Devices list is null - response format issue");
            return 0;
        }

        if (devices.isEmpty()) {
            log.debug("[HEALTH-SCORE] Room has no devices - empty room");
            return 100;
        }

        long activeCount = devices.stream().filter(DeviceDto::isActive).count();
        int score = (int) (((double) activeCount / devices.size()) * 100);
        log.debug("[HEALTH-SCORE] Calculated score: {}/{} devices active = {}%", 
                activeCount, devices.size(), score);
        return score;
    }
}
