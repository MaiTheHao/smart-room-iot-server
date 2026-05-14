package com.iviet.ivshs.schedule.rule.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.RuleDataSource;
import com.iviet.ivshs.schedule.rule.strategy.RuleDataSourceStrategy;

import java.time.Instant;
import java.time.ZoneId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemDataSourceStrategy implements RuleDataSourceStrategy {

  private static final String PROP_CURRENT_TIME = "current_time";
  private static final String PROP_DAY_OF_WEEK = "day_of_week";
  private static final String PROP_DAY_OF_MONTH = "day_of_month";

  @Override
  public boolean supports(RuleDataSource dataSource) {
    return RuleDataSource.SYSTEM.equals(dataSource);
  }

  @Override
  public Object fetchValue(RuleCondition condition, Long contextId) {
    if (condition == null || condition.getResourceParam() == null) {
      return null;
    }

    try {
      String property = condition.getResourceParam().path("property").asText(null);
      
      if (property == null) {
        log.warn("Property is missing in SYSTEM resourceParam for condition {}", condition.getId());
        return null;
      }

      Instant now = Instant.now();
      ZoneId utcZone = ZoneId.of("UTC");

      Object value = switch (property.toLowerCase()) {
        case PROP_CURRENT_TIME -> {
          var zonedDateTime = now.atZone(utcZone);
          yield zonedDateTime.getHour() + (zonedDateTime.getMinute() / 60.0);
        }
        case PROP_DAY_OF_WEEK -> now.atZone(utcZone).getDayOfWeek().getValue();
        case PROP_DAY_OF_MONTH -> now.atZone(utcZone).getDayOfMonth();
        default -> {
          log.warn("Property '{}' not supported for SYSTEM data source in condition {}", property, condition.getId());
          yield null;
        }
      };
      
      log.debug("Fetched SYSTEM data [UTC]: {} = {}", property, value);
      return value;

    } catch (Exception e) {
      log.error("Failed to provide SYSTEM data for condition {}: {}", condition.getId(), e.getMessage(), e);
      return null;
    }
  }
}