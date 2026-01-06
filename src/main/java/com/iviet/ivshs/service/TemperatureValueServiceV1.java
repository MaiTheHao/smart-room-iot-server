package com.iviet.ivshs.service;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.CreateTemperatureValueDto;
import com.iviet.ivshs.entities.Temperature;

public interface TemperatureValueServiceV1 {
	List<AverageTemperatureValueDto> getAverageTemperatureByRoom(Long roomId, Instant fromTimestamp, Instant toTimestamp);
	void create(CreateTemperatureValueDto dto);
	void createWithSensor(Temperature sensor, CreateTemperatureValueDto dto);
	void createBatchWithSensor(Temperature sensor, List<CreateTemperatureValueDto> dtoList);
}