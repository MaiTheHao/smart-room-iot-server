package com.iviet.ivshs.rule.strategy.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.RuleDataSource;
import com.iviet.ivshs.rule.strategy.DeviceStrategy;
import com.iviet.ivshs.rule.strategy.RuleDataSourceStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceDataSourceStrategy implements RuleDataSourceStrategy {

    private final List<DeviceStrategy> strategies;
    private final ObjectMapper objectMapper;

    @Override
    public Object provide(RuleCondition condition, Long contextId) {
        try {
            JsonNode params = objectMapper.readTree(condition.getResourceParam());
            if (params == null) {
                return null;
            }

            String categoryStr = params.path("category").asText();
            DeviceCategory category = DeviceCategory.valueOf(categoryStr);
            Long deviceId = params.path("deviceId").asLong();
            String property = params.path("property").asText();

            for (DeviceStrategy strategy : strategies) {
                if (strategy.supports(category)) {
                    return strategy.getValue(deviceId, property);
                }
            }
            log.warn("No device strategy found for category: {}", categoryStr);
            return null;
        } catch (Exception e) {
            log.error("Error in DeviceDataProvider: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean supports(RuleCondition condition) {
        return RuleDataSource.DEVICE.equals(condition.getDataSource());
    }
}
