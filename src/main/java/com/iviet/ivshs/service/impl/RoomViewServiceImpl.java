package com.iviet.ivshs.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.dto.RoomDetailViewModel;
import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDto;
import com.iviet.ivshs.service.AirConditionService;
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
	private final TemperatureValueService temperatureValueService;
	private final PowerConsumptionValueService powerConsumptionValueService;
	
	private static final long DEFAULT_MINUS_MINUTES = 15;
	private static final int MAX_DEVICES_PER_ROOM = 1000;

	@Override
	public RoomDetailViewModel getRoomDetailModel(Long roomId, String startedAtStr, String endedAtStr) {
		try {
			RoomDto room = getRoomMetadata(roomId);
			
			Instant now = Instant.now();
			Instant defaultStart = now.minus(DEFAULT_MINUS_MINUTES, ChronoUnit.MINUTES);
			Instant chartStart = parseInstant(startedAtStr, defaultStart);
			Instant chartEnd = parseInstant(endedAtStr, now);

			if (chartStart.isAfter(chartEnd)) {
				Instant temp = chartStart;
				chartStart = chartEnd;
				chartEnd = temp;
			}

			List<AverageTemperatureValueDto> tempChartData = getTemperatureChartData(roomId, chartStart, chartEnd);
			List<SumPowerConsumptionValueDto> powerChartData = getPowerChartData(roomId, chartStart, chartEnd);
			List<AverageTemperatureValueDto> currentTempData = getTemperatureChartData(roomId, defaultStart, now);
			List<SumPowerConsumptionValueDto> currentPowerData = getPowerChartData(roomId, defaultStart, now);

			Optional<Double> currentTemp = currentTempData.isEmpty() 
				? Optional.empty() 
				: Optional.of(currentTempData.getLast().avgTempC());
			Optional<Double> currentPower = currentPowerData.isEmpty() 
				? Optional.empty() 
				: Optional.of(currentPowerData.getLast().getSumWatt());
			
			List<LightDto> lights = getLightsForRoom(roomId);
			List<AirConditionDto> airConditions = getAirConditionsForRoom(roomId);

			return RoomDetailViewModel.builder()
					.room(room)
					.pageTitle(room.name())
					.currentTemp(currentTemp)
					.currentPower(currentPower)
					.tempChartData(tempChartData)
					.powerChartData(powerChartData)
					.lights(lights)
					.airConditions(airConditions)
					.errorMessage("")
					.build();

		} catch (Exception e) {
			log.error("Error building room detail model for room: {}", roomId, e);
			return buildErrorModel(roomId, e.getMessage());
		}
	}

	private RoomDto getRoomMetadata(Long roomId) {
		return roomService.getById(roomId);
	}

	private List<LightDto> getLightsForRoom(Long roomId) {
		try {
			return lightService.getListByRoomId(roomId, 0, MAX_DEVICES_PER_ROOM).content();
		} catch (Exception e) {
			log.error("Failed to load lights for room: {}", roomId, e);
			return Collections.emptyList();
		}
	}

	private List<AirConditionDto> getAirConditionsForRoom(Long roomId) {
		try {
			return airConditionService.getListByRoomId(roomId, 0, MAX_DEVICES_PER_ROOM).content();
		} catch (Exception e) {
			log.error("Failed to load air conditions for room: {}", roomId, e);
			return Collections.emptyList();
		}
	}

	private List<AverageTemperatureValueDto> getTemperatureChartData(Long roomId, Instant start, Instant end) {
		try {
			return temperatureValueService.getAverageTemperatureByRoom(roomId, start, end);
		} catch (Exception e) {
			log.error("Failed to load temperature data for room: {}", roomId, e);
			return Collections.emptyList();
		}
	}

	private List<SumPowerConsumptionValueDto> getPowerChartData(Long roomId, Instant start, Instant end) {
		try {
			return powerConsumptionValueService.getSumPowerConsumptionByRoom(roomId, start, end);
		} catch (Exception e) {
			log.error("Failed to load power data for room: {}", roomId, e);
			return Collections.emptyList();
		}
	}

	private RoomDetailViewModel buildErrorModel(Long roomId, String errorMessage) {
		try {
			RoomDto room = getRoomMetadata(roomId);
			return RoomDetailViewModel.builder()
					.room(room)
					.pageTitle("")
					.currentTemp(Optional.empty())
					.currentPower(Optional.empty())
					.tempChartData(Collections.emptyList())
					.powerChartData(Collections.emptyList())
					.lights(Collections.emptyList())
					.airConditions(Collections.emptyList())
					.errorMessage("Error: " + errorMessage)
					.build();
		} catch (Exception e) {
			log.error("Failed to build error model for room: {}", roomId, e);
			return RoomDetailViewModel.builder()
					.room(null)
					.pageTitle("")
					.currentTemp(Optional.empty())
					.currentPower(Optional.empty())
					.tempChartData(Collections.emptyList())
					.powerChartData(Collections.emptyList())
					.lights(Collections.emptyList())
					.airConditions(Collections.emptyList())
					.errorMessage("Critical Error: " + e.getMessage())
					.build();
		}
	}

	private Instant parseInstant(String val, Instant defaultVal) {
		if (val == null || val.isBlank()) {
			return defaultVal;
		}
		try {
			return val.matches("\\d+") 
				? Instant.ofEpochMilli(Long.parseLong(val)) 
				: Instant.parse(val);
		} catch (Exception e) {
			log.warn("Failed to parse instant from: {}, using default", val);
			return defaultVal;
		}
	}
}
