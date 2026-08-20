package com.iviet.ivshs.service.registry;

import com.iviet.ivshs.service.strategy.DeviceStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DeviceStateStrategyRegistry {

  private final Map<DeviceCategory, DeviceStateStrategy> strategies;

  public DeviceStateStrategyRegistry(List<DeviceStateStrategy> strategyList) {
    Map<DeviceCategory, DeviceStateStrategy> map = new EnumMap<>(DeviceCategory.class);

    for (DeviceStateStrategy strategy : strategyList) {
      for (DeviceCategory category : DeviceCategory.values()) {
        if (strategy.supports(category)) {
          DeviceStateStrategy existing = map.put(category, strategy);
          if (existing != null) {
            throw new IllegalStateException("Duplicate strategy detected for category '" + category
                + "': " + existing.getClass().getSimpleName() + " vs "
                + strategy.getClass().getSimpleName());
          }
        }
      }
    }

    this.strategies = Collections.unmodifiableMap(map);
  }

  public DeviceStateStrategy getStrategy(DeviceCategory category) {
    DeviceStateStrategy strategy = strategies.get(category);
    if (strategy == null) {
      throw new BadRequestException("Device state strategy not found for category: " + category);
    }
    return strategy;
  }

  public Optional<DeviceStateStrategy> findStrategy(DeviceCategory category) {
    return Optional.ofNullable(strategies.get(category));
  }

  public Object fetchState(DeviceCategory category, Long deviceId, String property) {
    if (category == null || deviceId == null || property == null) {
      return null;
    }
    return getStrategy(category).fetchState(deviceId, property);
  }
}
