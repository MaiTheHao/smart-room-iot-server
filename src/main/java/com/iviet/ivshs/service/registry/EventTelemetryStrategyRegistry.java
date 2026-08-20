package com.iviet.ivshs.service.registry;

import com.iviet.ivshs.dto.SensorEventRequestDto;
import com.iviet.ivshs.service.strategy.EventTelemetryStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EventTelemetryStrategyRegistry {

  private final Map<DeviceCategory, EventTelemetryStrategy> strategies;

  public EventTelemetryStrategyRegistry(List<EventTelemetryStrategy> strategyList) {
    Map<DeviceCategory, EventTelemetryStrategy> map = new EnumMap<>(DeviceCategory.class);

    for (EventTelemetryStrategy strategy : strategyList) {
      DeviceCategory category = strategy.getSupportedCategory();
      if (category != null) {
        EventTelemetryStrategy existing = map.put(category, strategy);
        if (existing != null) {
          throw new IllegalStateException("Duplicate strategy detected for category '"
              + category + "': " + existing.getClass().getSimpleName()
              + " vs " + strategy.getClass().getSimpleName());
        }
      }
    }

    this.strategies = Collections.unmodifiableMap(map);
  }

  public EventTelemetryStrategy getStrategy(DeviceCategory category) {
    EventTelemetryStrategy strategy = strategies.get(category);
    if (strategy == null) {
      throw new BadRequestException("Event telemetry strategy not found for category: " + category);
    }
    return strategy;
  }

  public Optional<EventTelemetryStrategy> findStrategy(DeviceCategory category) {
    return Optional.ofNullable(strategies.get(category));
  }

  public void processData(String naturalId, SensorEventRequestDto request) {
    if (request == null || request.getCategory() == null) {
      throw new BadRequestException("Request body or category cannot be null");
    }
    getStrategy(request.getCategory()).processData(naturalId, request);
  }
}
