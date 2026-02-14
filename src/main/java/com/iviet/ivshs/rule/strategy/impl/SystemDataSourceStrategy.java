package com.iviet.ivshs.rule.strategy.impl;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.RuleDataSource;
import com.iviet.ivshs.rule.strategy.RuleDataSourceStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemDataSourceStrategy implements RuleDataSourceStrategy {

    private final ObjectMapper objectMapper;

    private static final String PROPERTY_CURRENT_TIME = "current_time";

    @Override
    public Object provide(RuleCondition condition, Long contextId) {
        try {
            JsonNode params = objectMapper.readTree(condition.getResourceParam());
            if (params == null) {
                return null;
            }
            
            String property = params.path("property").asText();

            if (PROPERTY_CURRENT_TIME.equalsIgnoreCase(property)) {
                java.time.LocalTime now = java.time.LocalTime.now();
                return now.getHour() + (now.getMinute() / 60.0);
            }

            return null;
        } catch (Exception e) {
            log.error("Failed to provide SYSTEM data for condition {}: {}", 
                    condition != null ? condition.getId() : "null", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean supports(RuleCondition condition) {
        return RuleDataSource.SYSTEM.equals(condition.getDataSource());
    }
}
