package com.iviet.ivshs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonUtil {

    private static ObjectMapper mapper;

    @Autowired
    public void setMapper(ObjectMapper objectMapper) {
        JsonUtil.mapper = objectMapper;
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage(), e);
            throw new IllegalArgumentException("JSON serialization error", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            throw new IllegalArgumentException("JSON deserialization error", e);
        }
    }
}
