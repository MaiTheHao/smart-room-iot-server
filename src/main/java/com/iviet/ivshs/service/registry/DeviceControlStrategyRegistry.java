package com.iviet.ivshs.service.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dto.ControlDeviceResult;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeviceControlStrategyRegistry {

  private final Map<DeviceCategory, DeviceControlServiceStrategy<?>> strategies;
  private final ObjectMapper objectMapper;

  public DeviceControlStrategyRegistry(
      List<DeviceControlServiceStrategy<?>> strategyList, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    Map<DeviceCategory, DeviceControlServiceStrategy<?>> map = new EnumMap<>(DeviceCategory.class);

    for (DeviceControlServiceStrategy<?> strategy : strategyList) {
      DeviceCategory category = strategy.getSupportedCategory();
      DeviceControlServiceStrategy<?> existing = map.put(category, strategy);

      if (existing != null) {
        throw new IllegalStateException(
            "[DeviceControlStrategyRegistry] Duplicate strategy detected for category '"
                + category
                + "': "
                + existing.getClass().getSimpleName()
                + " vs "
                + strategy.getClass().getSimpleName());
      }

      log.info(
          "[DeviceControlStrategyRegistry] Registered '{}' -> {}",
          category,
          strategy.getClass().getSimpleName());
    }

    this.strategies = Collections.unmodifiableMap(map);
    log.info(
        "[DeviceControlStrategyRegistry] Initialized with {} categories: {}",
        strategies.size(),
        strategies.keySet());
  }

  public DeviceControlServiceStrategy<?> getStrategy(DeviceCategory category) {
    DeviceControlServiceStrategy<?> strategy = strategies.get(category);
    if (strategy == null) {
      throw new BadRequestException("Device control strategy not found for category: " + category);
    }
    return strategy;
  }

  public Optional<DeviceControlServiceStrategy<?>> findStrategy(DeviceCategory category) {
    return Optional.ofNullable(strategies.get(category));
  }

  public boolean supports(DeviceCategory category) {
    return strategies.containsKey(category);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public ControlDeviceResult executeControl(
      DeviceCategory category, Long deviceId, JsonNode params) {
    DeviceControlServiceStrategy strategy = getStrategy(category);
    try {
      Object controlDto = objectMapper.treeToValue(params, strategy.getControlDtoClass());
      return strategy.control(deviceId, controlDto);
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Invalid action parameters for category: " + category, e);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public ControlDeviceResult executeControl(
      DeviceCategory category, String naturalId, JsonNode params) {
    DeviceControlServiceStrategy strategy = getStrategy(category);
    try {
      Object controlDto = objectMapper.treeToValue(params, strategy.getControlDtoClass());
      return strategy.control(naturalId, controlDto);
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Invalid action parameters for category: " + category, e);
    }
  }
}
