package com.iviet.ivshs.service.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SensorStateStrategyRegistry {

  private final Map<DeviceCategory, SensorStateStrategy> strategies;

  public SensorStateStrategyRegistry(List<SensorStateStrategy> strategyList) {
    Map<DeviceCategory, SensorStateStrategy> map = new EnumMap<>(DeviceCategory.class);

    for (SensorStateStrategy strategy : strategyList) {
      for (DeviceCategory category : DeviceCategory.values()) {
        if (strategy.supports(category)) {
          SensorStateStrategy existing = map.put(category, strategy);
          if (existing != null) {
            throw new IllegalStateException(
                "[SensorStateStrategyRegistry] Duplicate strategy detected for category '" + category + "': "
                    + existing.getClass().getSimpleName() + " vs " + strategy.getClass().getSimpleName());
          }
          log.info("[SensorStateStrategyRegistry] Registered '{}' -> {}", category,
              strategy.getClass().getSimpleName());
        }
      }
    }

    this.strategies = Collections.unmodifiableMap(map);
    log.info("[SensorStateStrategyRegistry] Initialized with {} categories: {}", strategies.size(),
        strategies.keySet());
  }

  public SensorStateStrategy getStrategy(DeviceCategory category) {
    SensorStateStrategy strategy = strategies.get(category);
    if (strategy == null) {
      throw new BadRequestException("Sensor state strategy not found for category: " + category);
    }
    return strategy;
  }

  public Optional<SensorStateStrategy> findStrategy(DeviceCategory category) {
    return Optional.ofNullable(strategies.get(category));
  }

  public Object fetchState(DeviceCategory category, Long sensorId, String property) {
    if (category == null || sensorId == null || property == null) {
      return null;
    }
    return getStrategy(category).fetchState(sensorId, property);
  }
}
