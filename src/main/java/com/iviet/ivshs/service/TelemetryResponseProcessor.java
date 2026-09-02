package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.TelemetryProcessResult;
import com.iviet.ivshs.dto.TelemetryResponseDto;

public interface TelemetryResponseProcessor {

  TelemetryProcessResult process(TelemetryResponseDto data, String sourceIdentifier);
}
