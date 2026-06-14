package com.iviet.ivshs.service.powerconsumption;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.dto.powerconsumption.CreatePowerConsumptionValueDto;
import com.iviet.ivshs.dto.powerconsumption.SumPowerConsumptionValueDto;
import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.service.base.TelemetryCRUDServiceStrategy;

public interface PowerConsumptionValueService extends TelemetryCRUDServiceStrategy {
	List<SumPowerConsumptionValueDto> getSumPowerConsumptionByRoom(Long roomId, Instant from, Instant to);

	void create(CreatePowerConsumptionValueDto dto);

	void createWithSensor(PowerConsumption sensor, CreatePowerConsumptionValueDto dto);

	void createBatchWithSensor(PowerConsumption sensor, List<CreatePowerConsumptionValueDto> dtoList);
}
