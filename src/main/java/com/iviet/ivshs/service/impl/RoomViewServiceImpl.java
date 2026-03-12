package com.iviet.ivshs.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.RoomDetailViewModel;
import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDto;
import com.iviet.ivshs.service.AirConditionService;
import com.iviet.ivshs.service.FanService;
import com.iviet.ivshs.service.LightService;
import com.iviet.ivshs.service.PowerConsumptionValueService;
import com.iviet.ivshs.service.RoomService;
import com.iviet.ivshs.service.RoomViewService;
import com.iviet.ivshs.service.TemperatureValueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomViewServiceImpl implements RoomViewService {

	private final RoomService roomService;
	private final LightService lightService;
	private final AirConditionService airConditionService;
	private final FanService fanService;
	private final TemperatureValueService temperatureValueService;
	private final PowerConsumptionValueService powerConsumptionValueService;

	private static final long DEFAULT_MINUS_MINUTES = 15;
	private static final int MAX_DEVICES_PER_ROOM = 1000;

	@Override
	public RoomDetailViewModel getRoomDetailModel(Long roomId, String startedAtStr, String endedAtStr) {
		try {
			RoomDto room = roomService.getById(roomId);
			TimeRange chartRange = resolveTimeRange(startedAtStr, endedAtStr);
			
			Instant now = Instant.now();
			Instant defaultStart = now.minus(DEFAULT_MINUS_MINUTES, ChronoUnit.MINUTES);

			List<AverageTemperatureValueDto> tempChartData = getTemperatureChartData(roomId, chartRange.start(), chartRange.end());
			List<SumPowerConsumptionValueDto> powerChartData = getPowerChartData(roomId, chartRange.start(), chartRange.end());
			
			List<AverageTemperatureValueDto> currentTempData = getTemperatureChartData(roomId, defaultStart, now);
			List<SumPowerConsumptionValueDto> currentPowerData = getPowerChartData(roomId, defaultStart, now);

			return RoomDetailViewModel.builder()
				.room(room)
				.pageTitle(room.name())
				.currentTemp(currentTempData.isEmpty() ? Optional.empty() : Optional.of(currentTempData.getLast().avgTempC()))
				.currentPower(currentPowerData.isEmpty() ? Optional.empty() : Optional.of(currentPowerData.getLast().getSumWatt()))
				.tempChartData(tempChartData)
				.powerChartData(powerChartData)
				.lights(fetchSafe(() -> lightService.getListByRoomId(roomId, 0, MAX_DEVICES_PER_ROOM).content(), "lights", roomId))
				.airConditions(fetchSafe(() -> airConditionService.getListByRoomId(roomId, 0, MAX_DEVICES_PER_ROOM).content(), "AC", roomId))
				.fans(fetchSafe(() -> fanService.getListByRoomId(roomId, 0, MAX_DEVICES_PER_ROOM).content(), "fans", roomId))
				.errorMessage("")
				.build();

		} catch (Exception e) {
			log.error("Error building room detail model for room: {}", roomId, e);
			return buildErrorModel(roomId, e.getMessage());
		}
	}

	private <T> List<T> fetchSafe(Supplier<List<T>> fetcher, String label, Long roomId) {
		try {
			return fetcher.get();
		} catch (Exception e) {
			log.error("Failed to load {} for room: {}", label, roomId, e);
			return Collections.emptyList();
		}
	}

	private List<AverageTemperatureValueDto> getTemperatureChartData(Long roomId, Instant start, Instant end) {
		return fetchSafe(() -> temperatureValueService.getAverageTemperatureByRoom(roomId, start, end), "temp data", roomId);
	}

	private List<SumPowerConsumptionValueDto> getPowerChartData(Long roomId, Instant start, Instant end) {
		return fetchSafe(() -> powerConsumptionValueService.getSumPowerConsumptionByRoom(roomId, start, end), "power data", roomId);
	}

	private TimeRange resolveTimeRange(String startStr, String endStr) {
		Instant now = Instant.now();
		Instant start = parseInstant(startStr, now.minus(DEFAULT_MINUS_MINUTES, ChronoUnit.MINUTES));
		Instant end = parseInstant(endStr, now);
		return start.isAfter(end) ? new TimeRange(end, start) : new TimeRange(start, end);
	}

	private Instant parseInstant(String val, Instant defaultVal) {
		if (val == null || val.isBlank()) return defaultVal;
		try {
			return val.matches("\\d+") ? Instant.ofEpochMilli(Long.parseLong(val)) : Instant.parse(val);
		} catch (Exception e) {
			log.warn("Failed to parse instant from: {}, using default", val);
			return defaultVal;
		}
	}

	private RoomDetailViewModel buildErrorModel(Long roomId, String errorMessage) {
		RoomDto room = null;
		try {
			room = roomService.getById(roomId);
		} catch (Exception e) {
			log.error("Failed to fetch room metadata for error model: {}", roomId);
		}

		return RoomDetailViewModel.builder()
			.room(room)
			.pageTitle("")
			.currentTemp(Optional.empty())
			.currentPower(Optional.empty())
			.tempChartData(Collections.emptyList())
			.powerChartData(Collections.emptyList())
			.lights(Collections.emptyList())
			.airConditions(Collections.emptyList())
			.fans(Collections.emptyList())
			.errorMessage("Error: " + errorMessage)
			.build();
	}

	private record TimeRange(Instant start, Instant end) {}
}