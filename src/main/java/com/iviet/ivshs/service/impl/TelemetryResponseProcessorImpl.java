package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dto.TelemetryProcessResult;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.service.TelemetryResponseProcessor;
import com.iviet.ivshs.service.registry.TelemetryCRUDStrategyRegistry;
import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryResponseProcessorImpl implements TelemetryResponseProcessor {

  private final TelemetryCRUDStrategyRegistry strategyRegistry;

  @Override
  public TelemetryProcessResult process(TelemetryResponseDto data, String sourceIdentifier) {
    if (data == null || data.getData() == null || data.getData().getDevices() == null) {
      log.info("Source [{}]: No telemetry data available", sourceIdentifier);
      return TelemetryProcessResult.empty();
    }

    List<TelemetryResponseDto.DeviceDto> devices = data.getData().getDevices();
    int total = devices.size();
    int successCount = 0;
    int skippedCount = 0;
    int failedCount = 0;

    for (var deviceData : devices) {
      if (deviceData == null) {
        log.warn("Source [{}]: Skipping device with null data", sourceIdentifier);
        skippedCount++;
        continue;
      }

      if (deviceData.getCategory() == null) {
        log.warn("Source [{}]: Skipping device {} with null category",
          sourceIdentifier, deviceData.getNaturalId());

        skippedCount++;
        continue;
      }

      Optional<TelemetryCRUDServiceStrategy> strategyOpt = strategyRegistry.findStrategy(deviceData.getCategory());
      if (strategyOpt.isEmpty()) {
        log.warn("Source [{}]: No strategy for category {} at sensor {}",
          sourceIdentifier, deviceData.getCategory(), deviceData.getNaturalId());

        skippedCount++;
        continue;
      }

      try {
        strategyOpt.get().create(deviceData);
        successCount++;
      } catch (Exception e) {
        failedCount++;

        log.error("Source [{}]: Failed to process sensor {}: {}", 
          sourceIdentifier, deviceData.getNaturalId(), e.getMessage());
      }
    }

    log.info("Source [{}]: Processed {}/{} records (skipped: {}, failed: {})",
        sourceIdentifier, successCount, total, skippedCount, failedCount);

    return new TelemetryProcessResult(total, successCount, skippedCount, failedCount);
  }
}
