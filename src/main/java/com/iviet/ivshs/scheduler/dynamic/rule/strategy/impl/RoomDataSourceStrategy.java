package com.iviet.ivshs.scheduler.dynamic.rule.strategy.impl;

import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.dao.Co2MetricDao;
import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.HumidityMetricDao;
import com.iviet.ivshs.dao.LuxMetricDao;
import com.iviet.ivshs.dao.TemperatureDao;
import com.iviet.ivshs.dto.RoomCo2MetricDto;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.RuleDataSourceStrategy;
import com.iviet.ivshs.shared.enumeration.RuleDataSource;
import com.iviet.ivshs.shared.util.Calculator;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomDataSourceStrategy implements RuleDataSourceStrategy {

  private final TemperatureDao temperatureDao;
  private final EnergyMetricDao energyMetricDao;
  private final HumidityMetricDao humidityMetricDao;
  private final LuxMetricDao luxMetricDao;
  private final Co2MetricDao co2MetricDao;
  private final EngineProperties engineProperties;

  private static final String PROP_AVG_TEMPERATURE = "avg_temperature";
  private static final String PROP_SUM_WATT = "sum_watt";
  private static final String PROP_AVG_HUMIDITY = "avg_humidity";
  private static final String PROP_AVG_LUX = "avg_lux";
  private static final String PROP_AVG_CO2 = "avg_co2";
  private static final String PROP_MAX_CO2 = "max_co2";

  private int freshnessMinutes;

  @PostConstruct
  public void init() {
    freshnessMinutes = engineProperties.getRuleSensorFreshnessMinutes();
  }

  @Override
  public boolean supports(RuleDataSource dataSource) {
    return RuleDataSource.ROOM.equals(dataSource);
  }

  @Override
  public Object fetchValue(RuleCondition condition, Long contextId) {
    if (condition == null || condition.getResourceParam() == null) {
      log.debug("Condition or resource params are null");
      return null;
    }

    try {
      String property = condition.getResourceParam().path("property").asText(null);
      Long roomId = condition.getResourceParam().path("roomId").asLong(0L);

      if (property == null) {
        log.warn("Property is missing in ROOM resourceParam for condition {}", condition.getId());
        return null;
      }

      if (roomId == 0L) {
        log.warn(
            "roomId is missing or 0 in ROOM resourceParam for condition {}", condition.getId());
        return null;
      }

      Instant now = Instant.now();
      Instant thresholdTime = now.minus(freshnessMinutes, ChronoUnit.MINUTES);

      return switch (property.toLowerCase()) {
        case PROP_AVG_TEMPERATURE -> {
          List<Double> values = temperatureDao.findCurrentValuesByRoomId(roomId, thresholdTime);
          Double result = Calculator.median(values).orElse(null);
          log.debug(
              "Computed avg_temperature (median) for ROOM {}: {} (from {} sensors)",
              roomId,
              result,
              values.size());
          yield result;
        }
        case PROP_SUM_WATT -> {
          Double result =
              energyMetricDao.sumPowerByRoomId(roomId, thresholdTime).orElse(null);
          log.debug("Computed sum_watt for ROOM {}: {}", roomId, result);
          yield result;
        }
        case PROP_AVG_HUMIDITY -> {
          List<Double> values = humidityMetricDao.findCurrentValuesByRoomId(roomId, thresholdTime);
          Double result = Calculator.median(values).orElse(null);
          log.debug(
              "Computed avg_humidity (median) for ROOM {}: {} (from {} sensors)",
              roomId,
              result,
              values.size());
          yield result;
        }
        case PROP_AVG_LUX -> {
          List<Double> values = luxMetricDao.findCurrentValuesByRoomId(roomId, thresholdTime);
          Double result = Calculator.median(values).orElse(null);
          log.debug(
              "Computed avg_lux (median) for ROOM {}: {} (from {} sensors)",
              roomId,
              result,
              values.size());
          yield result;
        }
        case PROP_AVG_CO2 -> {
          var roomMetric = co2MetricDao.findLatestByRoomId(roomId, thresholdTime);
          Double result = roomMetric.map(RoomCo2MetricDto::getAvgCo2).orElse(null);
          log.debug("Computed avg_co2 (mean) for ROOM {}: {}", roomId, result);
          yield result;
        }
        case PROP_MAX_CO2 -> {
          var roomMetric = co2MetricDao.findLatestByRoomId(roomId, thresholdTime);
          Double result = roomMetric.map(RoomCo2MetricDto::getMaxCo2).orElse(null);
          log.debug("Computed max_co2 for ROOM {}: {}", roomId, result);
          yield result;
        }
        default -> {
          log.warn(
              "Property '{}' not supported for ROOM data source in condition {}",
              property,
              condition.getId());
          yield null;
        }
      };

    } catch (Exception e) {
      log.error(
          "Failed to provide ROOM data for condition {} (Room ID: {}): {}",
          condition.getId(),
          contextId,
          e.getMessage(),
          e);
      return null;
    }
  }
}
