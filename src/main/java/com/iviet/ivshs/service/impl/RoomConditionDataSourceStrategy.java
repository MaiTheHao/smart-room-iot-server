package com.iviet.ivshs.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.dao.Co2MetricDao;
import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.HumidityMetricDao;
import com.iviet.ivshs.dao.LuxMetricDao;
import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.ConditionValue;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.dto.RoomCo2MetricDto;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.service.strategy.ConditionDataSourceStrategy;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.shared.util.Calculator;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomConditionDataSourceStrategy implements ConditionDataSourceStrategy {

  private final TemperatureValueDao temperatureValueDao;
  private final EnergyMetricDao energyMetricDao;
  private final PowerConsumptionDao powerConsumptionDao;
  private final EngineProperties engineProperties;
  private final HumidityMetricDao humidityMetricDao;
  private final LuxMetricDao luxMetricDao;
  private final Co2MetricDao co2MetricDao;

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
  public ConditionDataSource getSupportedDataSource() {
    return ConditionDataSource.ROOM;
  }

  @Override
  public ConditionValue fetchValue(Condition condition, Long contextId) {
    if (condition == null || condition.getProperty() == null || condition.getSourceTargetId() == null) {
      log.debug("Condition, property, or sourceTargetId is null for condition: {}", condition != null ? condition.getId() : null);
      return new ConditionValue.MissingValue();
    }

    try {
      String property = condition.getProperty();
      Long roomId = Long.parseLong(condition.getSourceTargetId());

      Instant now = Instant.now();
      Instant startTime = now.minus(lookbackMinutes, ChronoUnit.MINUTES);

      return switch (property.toLowerCase()) {
        case PROP_AVG_TEMPERATURE -> {
          int divisor = TelemetryTimeGroup.getDivisorForRange(startTime, now);
          List<AverageTemperatureValueDto> history = temperatureValueDao.getAverageHistoryByRoom(roomId, startTime, now, divisor);
          log.debug("Fetched {} temperature records for ROOM {} in the last {} minutes (Condition: {})", history.size(), roomId, lookbackMinutes, condition.getId());
          var last = getLastElement(history);
          yield (last != null && last.avgTempC() != null)
              ? new ConditionValue.NumericValue(last.avgTempC())
              : new ConditionValue.MissingValue();
        }
        case PROP_SUM_WATT -> {
          List<Long> sensorIds = powerConsumptionDao.findActiveIdsByRoomId(roomId);
          if (sensorIds.isEmpty()) {
            log.warn("No active power consumption sensor found in room {}", roomId);
            yield new ConditionValue.MissingValue();
          }
          Long sensorId = sensorIds.get(0);
          int divisor = TelemetryTimeGroup.getDivisorForRange(startTime, now);
          List<EnergyMetricDto> history = energyMetricDao.findHistory(EnergyMetricCategory.ROOM, sensorId, startTime, now, divisor);
          log.debug("Fetched {} power records for ROOM {} (sensor {}) in the last {} minutes (Condition: {})", history.size(), roomId, sensorId, lookbackMinutes, condition.getId());
          var last = getLastElement(history);
          yield (last != null && last.getPower() != null)
              ? new ConditionValue.NumericValue(last.getPower())
              : new ConditionValue.MissingValue();
        }
        case PROP_AVG_HUMIDITY -> {
          List<Double> values = humidityMetricDao.findCurrentValuesByRoomId(roomId);
          Double result = Calculator.median(values).orElse(null);
          log.debug("Computed avg_humidity (median) for ROOM {}: {} (from {} sensors)", roomId, result, values.size());
          yield result != null ? new ConditionValue.NumericValue(result) : new ConditionValue.MissingValue();
        }
        case PROP_AVG_LUX -> {
          List<Double> values = luxMetricDao.findCurrentValuesByRoomId(roomId);
          Double result = Calculator.median(values).orElse(null);
          log.debug("Computed avg_lux (median) for ROOM {}: {} (from {} sensors)", roomId, result, values.size());
          yield result != null ? new ConditionValue.NumericValue(result) : new ConditionValue.MissingValue();
        }
        case PROP_AVG_CO2 -> {
          var roomMetric = co2MetricDao.findLatestByRoomId(roomId);
          Double result = roomMetric.map(RoomCo2MetricDto::getAvgCo2).orElse(null);
          log.debug("Computed avg_co2 (mean) for ROOM {}: {}", roomId, result);
          yield result != null ? new ConditionValue.NumericValue(result) : new ConditionValue.MissingValue();
        }
        case PROP_MAX_CO2 -> {
          var roomMetric = co2MetricDao.findLatestByRoomId(roomId);
          Double result = roomMetric.map(RoomCo2MetricDto::getMaxCo2).orElse(null);
          log.debug("Computed max_co2 for ROOM {}: {}", roomId, result);
          yield result != null ? new ConditionValue.NumericValue(result) : new ConditionValue.MissingValue();
        }
        default -> {
          log.warn("Property '{}' not supported for ROOM data source in condition {}", property, condition.getId());
          yield new ConditionValue.MissingValue();
        }
      };

    } catch (NumberFormatException e) {
      log.error("Invalid roomId '{}' in condition {}: {}", condition.getSourceTargetId(), condition.getId(), e.getMessage());
      return new ConditionValue.MissingValue();
    } catch (Exception e) {
      log.error("Failed to provide ROOM data for condition {} (Room ID: {}): {}", condition.getId(), condition.getSourceTargetId(), e.getMessage(), e);
      return new ConditionValue.MissingValue();
    }
  }

  private <T> T getLastElement(List<T> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    return list.get(list.size() - 1);
  }
}
