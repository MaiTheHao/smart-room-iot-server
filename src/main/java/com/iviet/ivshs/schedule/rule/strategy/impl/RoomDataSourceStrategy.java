package com.iviet.ivshs.schedule.rule.strategy.impl;

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
import com.iviet.ivshs.schedule.rule.strategy.RuleDataSourceStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomDataSourceStrategy implements RuleDataSourceStrategy {

	private final ObjectMapper objectMapper;
	private final TemperatureValueDao temperatureValueDao;
	private final PowerConsumptionValueDao powerConsumptionValueDao;

	private static final String PROP_TEMPERATURE = "temperature";
	private static final String PROP_WATT = "watt";
	private static final int LOOKBACK_MINUTES = 15;

	@Override
	public boolean supports(RuleCondition condition) {
		return condition != null && RuleDataSource.ROOM.equals(condition.getDataSource());
	}

	@Override
	public Object provide(RuleCondition condition, Long contextId) {
		if (condition == null || condition.getResourceParam() == null) {
			return null;
		}

		try {
			JsonNode params = objectMapper.readTree(condition.getResourceParam());
			String property = params.path("property").asText(null);
			
			if (property == null) {
				log.warn("Property is missing in ROOM resourceParam for condition {}", condition.getId());
				return null;
			}

			Instant now = Instant.now();
			Instant startTime = now.minus(LOOKBACK_MINUTES, ChronoUnit.MINUTES);

			return switch (property.toLowerCase()) {
				case PROP_TEMPERATURE -> {
					List<AverageTemperatureValueDto> history = temperatureValueDao.getAverageHistoryByRoom(contextId, startTime, now);
					yield getLastElement(history) != null ? getLastElement(history).avgTempC() : null;
				}
				case PROP_WATT -> {
					List<AveragePowerConsumptionValueDto> history = powerConsumptionValueDao.getAverageHistoryByRoom(contextId, startTime, now);
					yield getLastElement(history) != null ? getLastElement(history).getAvgWatt() : null;
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