package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.AverageTemperatureValueDtoV1;
import com.iviet.ivshs.dto.CreateTemperatureDtoV1;
import com.iviet.ivshs.dto.CreateTemperatureValueDtoV1;
import com.iviet.ivshs.dto.HealthCheckResponseDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.TemperatureDtoV1;
import com.iviet.ivshs.dto.UpdateTemperatureDtoV1;

import java.time.Instant;
import java.util.List;

public interface TemperatureServiceV1 {

    PaginatedResponseV1<TemperatureDtoV1> getListByRoom(Long roomId, int page, int size);

    TemperatureDtoV1 getById(Long tempSensorId);

    TemperatureDtoV1 create(CreateTemperatureDtoV1 dto);

    TemperatureDtoV1 update(Long tempSensorId, UpdateTemperatureDtoV1 dto);

    void delete(Long tempSensorId);

    void ingestSensorData(Long sensorId, CreateTemperatureValueDtoV1 dto);

    void ingestSensorDataBatch(Long sensorId, List<CreateTemperatureValueDtoV1> dtos);

    List<AverageTemperatureValueDtoV1> getAverageValueHistoryByRoomId(Long roomId, Instant startedAt, Instant endedAt);

    List<AverageTemperatureValueDtoV1> getAverageValueHistoryByClientId(Long clientId, Instant startedAt, Instant endedAt);

    int cleanupDataByRange(Long sensorId, Instant startedAt, Instant endedAt);

    HealthCheckResponseDtoV1 healthCheck(Long sensorId);
}
