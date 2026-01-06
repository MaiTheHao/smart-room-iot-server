package com.iviet.ivshs.service;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.dto.CreatePowerConsumptionValueDto;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDto;
import com.iviet.ivshs.entities.PowerConsumption;

public interface PowerConsumptionValueServiceV1 {
	List<SumPowerConsumptionValueDto> getSumPowerConsumptionByRoom(Long roomId, Instant fromTimestamp, Instant toTimestamp);
	void create(CreatePowerConsumptionValueDto dto);
	void createWithSensor(PowerConsumption sensor, CreatePowerConsumptionValueDto dto);
	void createBatchWithSensor(PowerConsumption sensor, List<CreatePowerConsumptionValueDto> dtoList);
}
