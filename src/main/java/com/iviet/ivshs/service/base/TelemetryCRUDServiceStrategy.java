package com.iviet.ivshs.service.base;

import com.iviet.ivshs.dto.metric.TelemetryResponseDto;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface TelemetryCRUDServiceStrategy {

  public DeviceCategory getSupportedCategory();

  public void create(TelemetryResponseDto.DeviceDto data);
}
