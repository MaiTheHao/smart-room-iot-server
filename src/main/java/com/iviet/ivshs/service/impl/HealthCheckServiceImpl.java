package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.HealthCheckResponseDto;
import com.iviet.ivshs.dto.HealthCheckResponseDto.DeviceDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.integration.gateway.GatewayAdapter;
import com.iviet.ivshs.integration.gateway.GatewayAdapterRegistry;
import com.iviet.ivshs.integration.gateway.GatewayOperationResult;
import com.iviet.ivshs.service.HealthCheckService;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.ExternalServiceException;
import com.iviet.ivshs.shared.exception.NetworkTimeoutException;
import com.iviet.ivshs.shared.util.MdcTaskWrapper;

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
public class HealthCheckServiceImpl implements HealthCheckService {

	private final ClientDao clientDao;
	private final RoomDao roomDao;
	private final GatewayAdapterRegistry gatewayAdapterRegistry;

	@Override
	public HealthCheckResponseDto checkByClient(Long clientId) {
		Client client = clientDao.findGatewayById(clientId)
				.orElseThrow(() -> new BadRequestException("Client not found ID: " + clientId));
		GatewayAdapter adapter = gatewayAdapterRegistry.get(client.getClientType());
		GatewayOperationResult healthResult = adapter.fetchHealthCheck(client.getIpAddress());

		if (!healthResult.success()) {
			return HealthCheckResponseDto.builder()
					.status(503)
					.message(healthResult.message())
					.timestamp(Instant.now().toString())
					.build();
		}
		return HealthCheckResponseDto.builder()
				.status(200)
				.message("OK")
				.timestamp(Instant.now().toString())
				.build();
	}

	@Override
	public HealthCheckResponseDto checkByClient(String ipAddress) {
		long start = System.currentTimeMillis();
		log.info("Starting health check for IP: {}", ipAddress);

		try {
			Client client = clientDao.findGatewayByIpAddress(ipAddress)
					.orElseThrow(() -> new BadRequestException("Client not found IP: " + ipAddress));
			GatewayAdapter adapter = gatewayAdapterRegistry.get(client.getClientType());
			GatewayOperationResult healthResult = adapter.fetchHealthCheck(ipAddress);

			if (!healthResult.success()) {
				log.warn("Health check not supported or failed for IP [{}]: {}", ipAddress, healthResult.message());
				return HealthCheckResponseDto.builder()
						.status(503)
						.message(healthResult.message())
						.timestamp(Instant.now().toString())
						.build();
			}

			log.info("Finished health check for IP: {} in {}ms", ipAddress, System.currentTimeMillis() - start);
			return HealthCheckResponseDto.builder()
					.status(200)
					.message("OK")
					.timestamp(Instant.now().toString())
					.build();
		} catch (ExternalServiceException e) {
			throw e;
		} catch (BadRequestException e) {
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error for IP [{}]: {}", ipAddress, e.getMessage());
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
		List<String> ipAddresses = clientDao.findGatewaysByRoomCode(roomCode)
				.stream()
				.map(Client::getIpAddress)
				.toList();

		if (ipAddresses.isEmpty()) {
			return Map.of();
		}

		long start = System.currentTimeMillis();
		log.info("Starting batch health check for room [{}] - {} gateways", roomCode, ipAddresses.size());

		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<CompletableFuture<Map.Entry<String, HealthCheckResponseDto>>> futures = ipAddresses.stream()
					.map(ip -> CompletableFuture.supplyAsync(MdcTaskWrapper.wrap(() -> {
						try {
							return Map.entry(ip, checkByClient(ip));
						} catch (Exception e) {
							String domainMessage = mapExceptionToMessage(e);
							return Map.entry(
									ip,
									HealthCheckResponseDto.builder()
											.status(500)
											.message(domainMessage)
											.timestamp(
													Instant.now()
															.toString())
											.build());
						}
					}), executor))
					.toList();

			Map<String, HealthCheckResponseDto> results = futures.stream()
					.map(CompletableFuture::join)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			log.info("Finished batch health check for room [{}] in {}ms", roomCode, System.currentTimeMillis() - start);
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
			log.warn("Client [{}] calculation failed: {}", clientId, e.getMessage());
			return 0;
		}
	}

	@Override
	public int getHealthScoreByRoom(Long roomId) {
		Map<String, HealthCheckResponseDto> roomResults = checkByRoom(roomId);

		if (roomResults.isEmpty())
			return 100;

		double averageScore = roomResults.values()
				.stream()
				.mapToInt(this::calculateScore)
				.average()
				.orElse(0.0);

		return (int) Math.round(averageScore);
	}

	private int calculateScore(HealthCheckResponseDto dto) {
		if (dto == null) {
			log.debug("Response is null - gateway error");
			return 0;
		}

		if (dto.getStatus() != 200) {
			log.debug("Response status {} != 200: {}", dto.getStatus(), dto.getMessage());
			return 0;
		}

		var healthData = dto.getData();
		if (healthData == null || healthData.getDevices() == null) {
			log.debug("Health data or devices list is null - response format issue");
			return 0;
		}

		List<DeviceDto> devices = healthData.getDevices();

		if (devices.isEmpty()) {
			log.debug("Room has no devices - empty room");
			return 100;
		}

		long activeCount = devices.stream()
				.filter(DeviceDto::isActive)
				.count();
		int score = (int) (((double) activeCount / devices.size()) * 100);
		log.debug("Calculated score: {}/{} devices active = {}%", activeCount, devices.size(), score);
		return score;
	}
}
