package com.iviet.ivshs.rule.strategy.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.PowerConsumptionValueDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.AveragePowerConsumptionValueDto;
import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.RuleDataSource;
import com.iviet.ivshs.rule.strategy.RuleDataSourceStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomDataSourceStrategy implements RuleDataSourceStrategy {

    private final ObjectMapper objectMapper;
    private final TemperatureValueDao temperatureValueDao;
    private final PowerConsumptionValueDao powerConsumptionValueDao;

    private static final String PROPERTY_TEMPERATURE = "temperature";
    private static final String PROPERTY_POWER_CONSUMPTION = "power_consumption";
    private static final String PROPERTY_WATT = "watt";

    @Override
    public Object provide(RuleCondition condition, Long contextId) {
        Long roomId = contextId;

        try {
            JsonNode params = objectMapper.readTree(condition.getResourceParam());
            if (params == null) {
                return null;
            }

            String property = params.path("property").asText();
            Instant now = Instant.now();
            Instant fifteenMinutesAgo = now.minus(15, ChronoUnit.MINUTES);

            if (PROPERTY_TEMPERATURE.equalsIgnoreCase(property)) {
                List<AverageTemperatureValueDto> history = temperatureValueDao.getAverageHistoryByRoom(roomId, fifteenMinutesAgo, now);
                if (history != null && !history.isEmpty()) {
                    return history.get(history.size() - 1).avgTempC();
                }
                return null;
            }

            if (PROPERTY_POWER_CONSUMPTION.equalsIgnoreCase(property) || PROPERTY_WATT.equalsIgnoreCase(property)) {
                List<AveragePowerConsumptionValueDto> history = powerConsumptionValueDao.getAverageHistoryByRoom(roomId, fifteenMinutesAgo, now);
                if (history != null && !history.isEmpty()) {
                    return history.get(history.size() - 1).getAvgWatt();
                }
                return null;
            }

            return null;
        } catch (Exception e) {
            log.error("Failed to provide ROOM data for condition {}: {}", condition.getId(), e.getMessage());
            return null;
        }
    }

    @Override
    public boolean supports(RuleCondition condition) {
        return RuleDataSource.ROOM.equals(condition.getDataSource());
    }
}

