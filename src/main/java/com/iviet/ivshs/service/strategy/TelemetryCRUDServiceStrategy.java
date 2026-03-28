package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.enumeration.DeviceCategory;

public interface TelemetryCRUDServiceStrategy {

  public DeviceCategory getSupportedCategory();

  public void create(TelemetryResponseDto.Data data);
}
