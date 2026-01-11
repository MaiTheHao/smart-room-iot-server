package com.iviet.ivshs.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.RoomDetailViewModel;
import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.service.PowerConsumptionValueService;
import com.iviet.ivshs.service.RoomViewProcessService;
import com.iviet.ivshs.service.RoomViewService;
import com.iviet.ivshs.service.TemperatureValueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomViewServiceImpl implements RoomViewService {

	private final RoomViewProcessService roomViewProcessService;
	private final TemperatureValueService temperatureValueService;
	private final PowerConsumptionValueService powerConsumptionValueService;
	private static final long DEFAULT_MINUS_MINUTES = 15;

	@Override
	public RoomDetailViewModel getModel(Long roomId, String startedAtStr, String endedAtStr) {
		RoomDto room = roomViewProcessService.getRoomMetadata(roomId);
		
		try {
			Instant now = Instant.now();
			Instant defaultStart = now.minus(DEFAULT_MINUS_MINUTES, ChronoUnit.MINUTES);

			Instant chartStart = parseInstant(startedAtStr, defaultStart);
			Instant chartEnd = parseInstant(endedAtStr, now);

			if (chartStart.isAfter(chartEnd)) {
				Instant temp = chartStart;
				chartStart = chartEnd;
				chartEnd = temp;
			}

			var tempChartData = temperatureValueService.getAverageTemperatureByRoom(roomId, chartStart, chartEnd);
			var powerChartData = powerConsumptionValueService.getSumPowerConsumptionByRoom(roomId, chartStart, chartEnd);
			var currentTempData = temperatureValueService.getAverageTemperatureByRoom(roomId, defaultStart, now);
			var currentPowerData = powerConsumptionValueService.getSumPowerConsumptionByRoom(roomId, defaultStart, now);

			Optional<Double> currentTemp = currentTempData.isEmpty() ? Optional.empty() : Optional.of(currentTempData.getLast().avgTempC());
			Optional<Double> currentPower = currentPowerData.isEmpty() ? Optional.empty() : Optional.of(currentPowerData.getLast().getSumWatt());
			
			var lights = roomViewProcessService.getLightsForRoom(roomId);

			return RoomDetailViewModel.builder()
					.room(room)
					.pageTitle(room.name())
					.currentTemp(currentTemp)
					.currentPower(currentPower)
					.tempChartData(tempChartData)
					.powerChartData(powerChartData)
					.lights(lights)
					.errorMessage("")
					.build();

		} catch (Exception e) {
			log.error("Error building room detail data for room: {}", roomId, e);
			return RoomDetailViewModel.builder()
					.errorMessage("Error: " + e.getMessage())
					.room(room)
					.pageTitle("")
					.currentTemp(Optional.empty())
					.currentPower(Optional.empty())
					.tempChartData(Collections.emptyList())
					.powerChartData(Collections.emptyList())
					.lights(Collections.emptyList())
					.build();
		}
	}

	@Override
	public void refreshRoomDetailData() {
		roomViewProcessService.evictAllCaches();
	}

	private Instant parseInstant(String val, Instant defaultVal) {
		if (val == null || val.isBlank()) return defaultVal;
		try {
			return val.matches("\\d+") ? Instant.ofEpochMilli(Long.parseLong(val)) : Instant.parse(val);
		} catch (Exception e) {
			return defaultVal;
		}
	}
}
