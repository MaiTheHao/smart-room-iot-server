package com.iviet.ivshs.service.factory;

import com.iviet.ivshs.dto.ConditionValue;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.service.strategy.ConditionDataSourceStrategy;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
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
public class ConditionDataSourceFactory {

  private final Map<ConditionDataSource, ConditionDataSourceStrategy> strategies;

  public ConditionDataSourceFactory(List<ConditionDataSourceStrategy> strategyList) {
    Map<ConditionDataSource, ConditionDataSourceStrategy> map =
        new EnumMap<>(ConditionDataSource.class);

    for (ConditionDataSourceStrategy strategy : strategyList) {
      ConditionDataSource dataSource = strategy.getSupportedDataSource();
      ConditionDataSourceStrategy existing = map.put(dataSource, strategy);

      if (existing != null) {
        throw new IllegalStateException(
            "[ConditionDataSourceFactory] Duplicate strategy detected for dataSource '" + dataSource
                + "': "
                + existing.getClass().getSimpleName() + " vs "
                + strategy.getClass().getSimpleName()
                + ". Only one ConditionDataSourceStrategy per dataSource is allowed.");
      }

      log.info(
          "[ConditionDataSourceFactory] Registered '{}' -> {}",
          dataSource,
          strategy.getClass().getSimpleName());
    }

    this.strategies = Collections.unmodifiableMap(map);
    log.info(
        "[ConditionDataSourceFactory] Initialized with {} data sources: {}",
        strategies.size(),
        strategies.keySet());
  }

  public ConditionDataSourceStrategy getStrategy(ConditionDataSource dataSource) {
    ConditionDataSourceStrategy strategy = strategies.get(dataSource);
    if (strategy == null) {
      throw new BadRequestException("Condition data source " + dataSource + " is not supported");
    }
    return strategy;
  }

  public Optional<ConditionDataSourceStrategy> findStrategy(ConditionDataSource dataSource) {
    return Optional.ofNullable(strategies.get(dataSource));
  }

  public ConditionValue fetchValue(Condition condition, Long contextId) {
    if (condition == null || condition.getSourceCategory() == null) {
      log.debug("[ConditionDataSourceFactory] Condition or sourceCategory is null");
      return new ConditionValue.MissingValue();
    }
    return getStrategy(condition.getSourceCategory()).fetchValue(condition, contextId);
  }
}
