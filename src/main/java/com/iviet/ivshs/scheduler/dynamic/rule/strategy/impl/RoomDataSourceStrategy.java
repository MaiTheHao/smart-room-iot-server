package com.iviet.ivshs.scheduler.dynamic.rule.strategy.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.RuleDataSourceStrategy;
import com.iviet.ivshs.shared.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.shared.enumeration.RuleDataSource;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.iviet.ivshs.dao.HumidityMetricDao;
import com.iviet.ivshs.dao.LuxMetricDao;
import com.iviet.ivshs.dao.Co2MetricDao;
import com.iviet.ivshs.dto.RoomCo2MetricDto;
import com.iviet.ivshs.shared.util.Calculator;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomDataSourceStrategy implements RuleDataSourceStrategy {

  private final TemperatureValueDao temperatureValueDao;
  private final EnergyMetricDao energyMetricDao;
  private final EngineProperties engineProperties;
  private final HumidityMetricDao humidityMetricDao;
  private final LuxMetricDao luxMetricDao;
  private final Co2MetricDao co2MetricDao;

  @PersistenceContext
  private EntityManager entityManager;

  private static final String PROP_AVG_TEMPERATURE = "avg_temperature";
  private static final String PROP_SUM_WATT = "sum_watt";
  private static final String PROP_AVG_HUMIDITY = "avg_humidity";
  private static final String PROP_AVG_LUX = "avg_lux";
  private static final String PROP_AVG_CO2 = "avg_co2";
  private static final String PROP_MAX_CO2 = "max_co2";

  private int lookbackMinutes;

  @PostConstruct
  public void init() {
    lookbackMinutes = engineProperties.getRuleTelemetryLookbackMinutes();
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
        log.warn("roomId is missing or 0 in ROOM resourceParam for condition {}", condition.getId());
        return null;
      }

      Instant now = Instant.now();
      Instant startTime = now.minus(lookbackMinutes, ChronoUnit.MINUTES);

      return switch (property.toLowerCase()) {
        case PROP_AVG_TEMPERATURE -> {
          int divisor = TelemetryTimeGroup.getDivisorForRange(startTime, now);
          List<AverageTemperatureValueDto> history = temperatureValueDao.getAverageHistoryByRoom(roomId, startTime, now, divisor);
          log.debug("Fetched {} temperature records for ROOM {} in the last {} minutes (Condition: {})", history.size(), roomId, lookbackMinutes, condition.getId());
          yield getLastElement(history) != null ? getLastElement(history).avgTempC() : null;
        }
        case PROP_SUM_WATT -> {
          List<Long> sensorIds = entityManager.createQuery(
              "SELECT pc.id FROM PowerConsumption pc WHERE pc.room.id = :roomId AND pc.isActive = true", Long.class)
              .setParameter("roomId", roomId)
              .getResultList();
          if (sensorIds.isEmpty()) {
            log.warn("No active power consumption sensor found in room {}", roomId);
            yield null;
          }
          Long sensorId = sensorIds.get(0);
          int divisor = TelemetryTimeGroup.getDivisorForRange(startTime, now);
          List<EnergyMetricDto> history = energyMetricDao.findHistory(EnergyMetricCategory.ROOM, sensorId, startTime, now, divisor);
          log.debug("Fetched {} power records for ROOM {} (sensor {}) in the last {} minutes (Condition: {})", history.size(), roomId, sensorId, lookbackMinutes, condition.getId());
          yield getLastElement(history) != null ? getLastElement(history).getPower() : null;
        }
        case PROP_AVG_HUMIDITY -> {
            List<Double> values = humidityMetricDao.findCurrentValuesByRoomId(roomId);
            Double result = Calculator.median(values).orElse(null);
            log.debug("Computed avg_humidity (median) for ROOM {}: {} (from {} sensors)", roomId, result, values.size());
            yield result;
        }
        case PROP_AVG_LUX -> {
            List<Double> values = luxMetricDao.findCurrentValuesByRoomId(roomId);
            Double result = Calculator.median(values).orElse(null);
            log.debug("Computed avg_lux (median) for ROOM {}: {} (from {} sensors)", roomId, result, values.size());
            yield result;
        }
        case PROP_AVG_CO2 -> {
            var roomMetric = co2MetricDao.findLatestByRoomId(roomId);
            Double result = roomMetric.map(RoomCo2MetricDto::getAvgCo2).orElse(null);
            log.debug("Computed avg_co2 (mean) for ROOM {}: {}", roomId, result);
            yield result;
        }
        case PROP_MAX_CO2 -> {
            var roomMetric = co2MetricDao.findLatestByRoomId(roomId);
            Double result = roomMetric.map(RoomCo2MetricDto::getMaxCo2).orElse(null);
            log.debug("Computed max_co2 for ROOM {}: {}", roomId, result);
            yield result;
        }
        default -> {
          log.warn("Property '{}' not supported for ROOM data source in condition {}", property, condition.getId());
          yield null;
        }
      };

    } catch (Exception e) {
      log.error("Failed to provide ROOM data for condition {} (Room ID: {}): {}", condition.getId(), contextId, e.getMessage(), e);
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
