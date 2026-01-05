package com.iviet.ivshs.service;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.dto.AverageTemperatureValueDtoV1;
import com.iviet.ivshs.dto.CreateTemperatureValueDtoV1;
import com.iviet.ivshs.entities.TemperatureV1;

public interface TemperatureValueServiceV1 {
	List<AverageTemperatureValueDtoV1> getAverageTemperatureByRoom(Long roomId, Instant fromTimestamp, Instant toTimestamp);
	void create(CreateTemperatureValueDtoV1 dto);
	void createWithSensor(TemperatureV1 sensor, CreateTemperatureValueDtoV1 dto);
	void createBatchWithSensor(TemperatureV1 sensor, List<CreateTemperatureValueDtoV1> dtoList);
}