package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.AveragePowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.CreatePowerConsumptionDtoV1;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.HealthCheckRequestDtoV1;
import com.iviet.ivshs.dto.HealthCheckResponseDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.PowerConsumptionDtoV1;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.UpdatePowerConsumptionDtoV1;

import java.time.Instant;
import java.util.List;

public interface PowerConsumptionServiceV1 {

    PaginatedResponseV1<PowerConsumptionDtoV1> getListByRoom(Long roomId, int page, int size);

    PowerConsumptionDtoV1 getById(Long powerSensorId);

    PowerConsumptionDtoV1 create(CreatePowerConsumptionDtoV1 dto);

    PowerConsumptionDtoV1 update(Long powerSensorId, UpdatePowerConsumptionDtoV1 dto);

    void delete(Long powerSensorId);

    void ingestSensorData(Long sensorId, CreatePowerConsumptionValueDtoV1 dto);

    void ingestSensorDataBatch(Long sensorId, List<CreatePowerConsumptionValueDtoV1> dtos);

    List<AveragePowerConsumptionValueDtoV1> getAverageValueHistoryByRoomId(Long roomId, Instant startedAt, Instant endedAt);

    List<SumPowerConsumptionValueDtoV1> getSumValueHistoryByRoomId(Long roomId, Instant startedAt, Instant endedAt);

    List<AveragePowerConsumptionValueDtoV1> getAverageValueHistoryByClientId(Long clientId, Instant startedAt, Instant endedAt);

    List<SumPowerConsumptionValueDtoV1> getSumValueHistoryByClientId(Long clientId, Instant startedAt, Instant endedAt);

    int cleanupDataByRange(Long sensorId, Instant startedAt, Instant endedAt);

    HealthCheckResponseDtoV1 healthCheck(Long sensorId);
}
