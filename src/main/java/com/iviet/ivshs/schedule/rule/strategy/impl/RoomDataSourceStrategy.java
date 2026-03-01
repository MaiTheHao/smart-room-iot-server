package com.iviet.ivshs.schedule.rule.strategy.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.PowerConsumptionValueDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDto;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.RuleDataSource;
import com.iviet.ivshs.schedule.rule.strategy.RuleDataSourceStrategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomDataSourceStrategy implements RuleDataSourceStrategy {

  private final TemperatureValueDao temperatureValueDao;
  private final PowerConsumptionValueDao powerConsumptionValueDao;
  private final Environment env;

  private static final String PROP_TEMPERATURE = "temperature";
  private static final String PROP_WATT = "watt";
  private static final int DEFAULT_LOOKBACK_MINUTES = 24 * 60; // 24 hours by default
  
  private int lookbackMinutes;

  @PostConstruct
  public void init() {
    lookbackMinutes = env.getProperty("app.engine.rule.telemetryLookbackMinutes", Integer.class, DEFAULT_LOOKBACK_MINUTES);
  }

  @Override
  public boolean supports(RuleDataSource dataSource) {
    return RuleDataSource.ROOM.equals(dataSource);
  }

  @Override
  public Object fetchValue(RuleCondition condition, Long contextId) {
    if (condition == null || condition.getResourceParam() == null) {
      return null;
    }

    try {
      String property = condition.getResourceParam().path("property").asText(null);

      if (property == null) {
        log.warn("Property is missing in ROOM resourceParam for condition {}", condition.getId());
        return null;
      }

      Instant now = Instant.now();
      Instant startTime = now.minus(lookbackMinutes, ChronoUnit.MINUTES);

      return switch (property.toLowerCase()) {
        case PROP_TEMPERATURE -> {
          List<AverageTemperatureValueDto> history = temperatureValueDao.getAverageHistoryByRoom(contextId, startTime, now);
          log.info("Fetched {} temperature records for ROOM {} in the last {} minutes", history.size(), contextId, lookbackMinutes);
          yield getLastElement(history) != null ? getLastElement(history).avgTempC() : null;
        }
        case PROP_WATT -> {
          List<SumPowerConsumptionValueDto> history = powerConsumptionValueDao.getSumHistoryByRoom(contextId, startTime, now);
          log.info("Fetched {} watt records for ROOM {} in the last {} minutes", history.size(), contextId, lookbackMinutes);
          yield getLastElement(history) != null ? getLastElement(history).getSumWatt() : null;
        }
        default -> {
          log.warn("Property {} not supported for ROOM data source in condition {}", property, condition.getId());
          yield null;
        }
      };

    } catch (Exception e) {
      log.error("Failed to provide ROOM data for condition {} (Room ID: {}): {}",
          condition.getId(), contextId, e.getMessage());
      return null;
    }
  }

  private <T> T getLastElement(List<T> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    return list.get(list.size() - 1);
  }
}
