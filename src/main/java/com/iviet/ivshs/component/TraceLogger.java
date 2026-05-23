package com.iviet.ivshs.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TraceLogger {

    private final ObjectMapper objectMapper;

    public void logTrace(String traceId, String scenarioId, String type, String method,
            String uri, Integer status, long durationMs, String controller, String remote) {
        if (log.isTraceEnabled()) {
            try {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("traceId", traceId != null ? traceId : "");
                m.put("scenarioId", scenarioId != null ? scenarioId : "");
                m.put("type", type);
                m.put("method", method);
                m.put("uri", uri);
                m.put("status", status);
                m.put("duration_ms", durationMs);
                m.put("controller", controller);
                m.put("remote", remote);
                log.trace(objectMapper.writeValueAsString(m));
            } catch (Exception e) {
                log.trace("{}");
            }
        }
    }
}
