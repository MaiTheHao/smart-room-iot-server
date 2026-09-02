package com.iviet.ivshs.service.registry;

import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TelemetryCRUDStrategyRegistry {

  private final Map<DeviceCategory, TelemetryCRUDServiceStrategy> strategies;

  public TelemetryCRUDStrategyRegistry(List<TelemetryCRUDServiceStrategy> strategyList) {
    Map<DeviceCategory, TelemetryCRUDServiceStrategy> map = new EnumMap<>(DeviceCategory.class);

    for (TelemetryCRUDServiceStrategy strategy : strategyList) {
      DeviceCategory category = strategy.getSupportedCategory();
      if (category != null) {
        TelemetryCRUDServiceStrategy existing = map.put(category, strategy);
        if (existing != null) {
          throw new IllegalStateException(
              "Duplicate telemetry CRUD strategy detected for category '" + category
                  + "': " + existing.getClass().getSimpleName() + " vs "
                  + strategy.getClass().getSimpleName());
        }
      }
    }
    this.strategies = Collections.unmodifiableMap(map);
  }

  public Optional<TelemetryCRUDServiceStrategy> findStrategy(DeviceCategory category) {
    if (category == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(strategies.get(category));
  }
}
