package com.iviet.ivshs.service.strategy.impl;

import java.time.Instant;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dto.ConditionValue;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.service.strategy.ConditionDataSourceStrategy;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConditionDataSourceStrategy implements ConditionDataSourceStrategy {

  private static final String PROP_CURRENT_TIME = "current_time";
  private static final String PROP_DAY_OF_WEEK = "day_of_week";
  private static final String PROP_DAY_OF_MONTH = "day_of_month";

  @Override
  public ConditionDataSource getSupportedDataSource() {
    return ConditionDataSource.SYSTEM;
  }

  @Override
  public ConditionValue fetchValue(Condition condition, Long contextId) {
    if (condition == null || condition.getProperty() == null) {
      log.debug("Condition or property is null for condition: {}", condition != null ? condition.getId() : null);
      return new ConditionValue.MissingValue();
    }

    try {
      String property = condition.getProperty();
      Instant now = Instant.now();
      ZoneId utcZone = ZoneId.of("UTC");

      ConditionValue value = switch (property.toLowerCase()) {
        case PROP_CURRENT_TIME -> {
          var zonedDateTime = now.atZone(utcZone);
          yield new ConditionValue.NumericValue(zonedDateTime.getHour() + (zonedDateTime.getMinute() / 60.0));
        }
        case PROP_DAY_OF_WEEK -> new ConditionValue.NumericValue(now.atZone(utcZone).getDayOfWeek().getValue());
        case PROP_DAY_OF_MONTH -> new ConditionValue.NumericValue(now.atZone(utcZone).getDayOfMonth());
        default -> {
          log.warn("Property '{}' not supported for SYSTEM data source in condition {}", property, condition.getId());
          yield new ConditionValue.MissingValue();
        }
      };

      log.debug("Fetched SYSTEM data [UTC]: {} = {}", property, value);
      return value;

    } catch (Exception e) {
      log.error("Failed to provide SYSTEM data for condition {}: {}", condition.getId(), e.getMessage(), e);
      return new ConditionValue.MissingValue();
    }
  }
}
