package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.shared.util.MdcTaskWrapper;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.service.RoomService;
import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;
import com.iviet.ivshs.service.TelemetryService;
import com.iviet.ivshs.service.ClientService;
import com.iviet.ivshs.integration.gateway.GatewayAdapter;
import com.iviet.ivshs.integration.gateway.GatewayAdapterRegistry;
import com.iviet.ivshs.integration.gateway.GatewayFetchResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService {

	private final ClientService clientService;
	private final RoomService roomService;
	private final List<TelemetryCRUDServiceStrategy> strategies;
	private final GatewayAdapterRegistry gatewayAdapterRegistry;
	private final Map<DeviceCategory, TelemetryCRUDServiceStrategy> strategyMap = new EnumMap<>(DeviceCategory.class);

	@PostConstruct
	private void init() {
		strategies.forEach(s -> strategyMap.put(s.getSupportedCategory(), s));
		log.info("Initialized with {} strategies", strategyMap.size());
	}

	@Override
	public void takeByGateway(String gatewayUsername) {
		processTakeByGateway(clientService.getGatewayByUsername(gatewayUsername));
	}

	@Override
	public void takeByGateway(Long gatewayId) {
		processTakeByGateway(clientService.getGatewayById(gatewayId));
	}

	@Override
	public void takeByIpAddress(String gatewayIpAddress) {
		processTakeByGateway(clientService.getGatewayByIpAddress(gatewayIpAddress));
	}

	@Override
	public void takeByRoom(Long roomId) {
		List<ClientDto> gateways = clientService.getListGatewaysByRoomId(roomId, 0, 1000)
				.content();
		executeBatchProcessing(gateways, String.valueOf(roomId));
	}

	@Override
	public void takeByRoom(String roomCode) {
		if (roomCode == null || roomCode.isBlank()) {
			throw new BadRequestException("Room code is required");
		}
		Long roomId = roomService.getEntityByCode(roomCode)
				.getId();
		List<ClientDto> gateways = clientService.getListGatewaysByRoomId(roomId, 0, 1000)
				.content();
		executeBatchProcessing(gateways, roomCode);
	}

	@Override
	public void takeGlobalTelemetry() {
		log.info("Starting global telemetry collection");
		long start = System.currentTimeMillis();
		try {
			List<ClientDto> gateways = clientService.getAllGateways();
			gateways.forEach(this::processSafeExecution);
			log.info("Finished global telemetry collection in {}ms", System.currentTimeMillis() - start);
		} catch (Exception e) {
			log.error("Failed to collect global telemetry: {}", e.getMessage(), e);
		}
	}

	private void executeBatchProcessing(List<ClientDto> gateways, String identifier) {
		if (gateways == null || gateways.isEmpty())
			return;

		long start = System.currentTimeMillis();
		log.info("Starting batch collection for [{}] - {} gateways", identifier, gateways.size());

		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<CompletableFuture<Void>> futures = gateways.stream()
					.map(gateway -> CompletableFuture.runAsync(MdcTaskWrapper.wrap(() -> processSafeExecution(gateway)), executor))
					.toList();

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
					.join();
			log.info("Finished batch collection for [{}] in {}ms", identifier, System.currentTimeMillis() - start);
		}
	}

	private void processSafeExecution(ClientDto gateway) {
		try {
			processTakeByGateway(gateway);
		} catch (Exception e) {
			log.error("Failed to process gateway [{}]: {}", gateway.username(), e.getMessage());
		}
	}

	protected void processTakeByGateway(ClientDto gateway) {
		GatewayAdapter adapter = gatewayAdapterRegistry.get(gateway.clientType());
		GatewayFetchResult<TelemetryResponseDto> result = adapter.fetchGlobalTelemetry(gateway.ipAddress());

		if (!result.success()) {
			log.debug("Global telemetry skipped for gateway [{}]: {}",
				gateway.username(), result.message());
			return;
		}

		result.getData().ifPresent(data -> processTelemetryData(gateway, data));
	}

	private void processTelemetryData(ClientDto gateway, TelemetryResponseDto data) {
		if (data == null || data.getData() == null || data.getData().getDevices() == null) {
			log.info("Gateway [{}]: No telemetry data available", gateway.username());
			return;
		}

		List<TelemetryResponseDto.DeviceDto> telemetryData = data.getData().getDevices();
		int processedCount = 0;

		for (var deviceData : telemetryData) {
			try {
				TelemetryCRUDServiceStrategy strategy = strategyMap.get(deviceData.getCategory());
				if (strategy != null) {
					strategy.create(deviceData);
					processedCount++;
				} else {
					log.warn("No strategy for category {} at sensor {}", deviceData.getCategory(), deviceData.getNaturalId());
				}
			} catch (Exception e) {
				log.error("Failed to process sensor {} for gateway {}: {}", deviceData.getNaturalId(), gateway.username(), e.getMessage());
			}
		}
		log.info("Gateway {}: Processed {}/{} records", gateway.username(), processedCount, telemetryData.size());
	}
}
