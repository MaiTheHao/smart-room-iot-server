package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.ControlDeviceResponseV1;
import com.iviet.ivshs.dto.CreateLightDtoV1;
import com.iviet.ivshs.dto.HealthCheckResponseDtoV1;
import com.iviet.ivshs.dto.LightDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.UpdateLightDtoV1;

public interface LightServiceV1 {

    PaginatedResponseV1<LightDtoV1> getList(int page, int size);

    PaginatedResponseV1<LightDtoV1> getListByRoomId(Long roomId, int page, int size);

    LightDtoV1 getById(Long lightId);

    LightDtoV1 create(CreateLightDtoV1 createDto);

    LightDtoV1 update(Long lightId, UpdateLightDtoV1 updateDto);

    void delete(Long lightId);

    ControlDeviceResponseV1 toggleState(Long lightId);

    ControlDeviceResponseV1 setLevel(Long lightId, int newLevel);

    HealthCheckResponseDtoV1 healthCheck(Long lightId);
}
