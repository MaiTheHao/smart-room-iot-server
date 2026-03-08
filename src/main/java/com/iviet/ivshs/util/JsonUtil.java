package com.iviet.ivshs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    /**
     * Parse a JSON string into a JsonNode.
     * Returns NullNode if input is null or blank.
     */
    public static JsonNode parse(String json) {
        if (json == null || json.isBlank()) return NullNode.getInstance();
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            log.error("Failed to parse JSON string: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Convert an object to a JsonNode.
     * Returns NullNode if input is null.
     */
    public static JsonNode toNode(Object obj) {
        if (obj == null) return NullNode.getInstance();
        return mapper.valueToTree(obj);
    }

    /**
     * Serialize a JsonNode to a JSON string.
     */
    public static String stringify(JsonNode node) {
        if (node == null) return null;
        try {
            return mapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("Failed to stringify JsonNode: {}", e.getMessage(), e);
            throw new IllegalArgumentException("JSON serialization error", e);
        }
    }

    /**
     * Safely parse a JSON string, returning Optional.empty() on failure.
     */
    public static Optional<JsonNode> tryParse(String json) {
        try {
            return Optional.of(parse(json));
        } catch (Exception e) {
            log.warn("Failed to safely parse JSON: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
