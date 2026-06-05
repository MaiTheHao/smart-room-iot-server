package com.iviet.ivshs.service.temperature;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.dto.temperature.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.temperature.CreateTemperatureValueDto;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.service.metric.strategy.TelemetryCRUDServiceStrategy;

public interface TemperatureValueService extends TelemetryCRUDServiceStrategy {
	List<AverageTemperatureValueDto> getAverageTemperatureByRoom(Long roomId, Instant from, Instant to);

	void create(CreateTemperatureValueDto dto);

	void createWithSensor(Temperature sensor, CreateTemperatureValueDto dto);

	void createBatchWithSensor(Temperature sensor, List<CreateTemperatureValueDto> dtoList);
}