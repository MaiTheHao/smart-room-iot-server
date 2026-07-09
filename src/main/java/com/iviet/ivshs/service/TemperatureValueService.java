package com.iviet.ivshs.service.temperature;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.CreateTemperatureValueDto;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.service.base.TelemetryCRUDServiceStrategy;
import com.iviet.ivshs.service.telemetry.SensorTelemetryServiceStrategy;

public interface TemperatureValueService extends TelemetryCRUDServiceStrategy, SensorTelemetryServiceStrategy {
	
	List<AverageTemperatureValueDto> getAverageTemperatureByRoom(Long roomId, Instant from, Instant to);

	void create(CreateTemperatureValueDto dto);

	void createWithSensor(Temperature sensor, CreateTemperatureValueDto dto);

	void createBatchWithSensor(Temperature sensor, List<CreateTemperatureValueDto> dtoList);
}
