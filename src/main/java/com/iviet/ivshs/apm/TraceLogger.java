package com.iviet.ivshs.apm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class TraceLogger {

    private final ObjectMapper objectMapper;

    public void logTrace(TraceData traceData) {
        if (log.isTraceEnabled()) {
            try {
                log.trace(objectMapper.writeValueAsString(traceData));
            } catch (Exception e) {
                log.trace("{}");
            }
        }
    }

    @Getter
    public static class TraceData {
        private final String traceId;
        private final String scenarioId;
        private final String type;
        private final String method;
        private final String uri;
        private final Integer status;

        @JsonProperty("duration_ms")
        private final Long durationMs;

        private final String controller;
        private final String remote;

        @JsonProperty("started_at")
        private final Instant startedAt;

        @JsonProperty("ended_at")
        private final Instant endedAt;

        @Builder
        public TraceData(String traceId, String scenarioId, String type, String method,
                String uri, Integer status, String controller,
                String remote, Instant startedAt, Instant endedAt) {
            this.traceId = traceId != null ? traceId : "";
            this.scenarioId = scenarioId != null ? scenarioId : "";
            this.type = type;
            this.method = method;
            this.uri = uri;
            this.status = status;
            this.controller = controller;
            this.remote = remote;
            this.startedAt = startedAt;
            this.endedAt = endedAt;
            this.durationMs = (startedAt != null && endedAt != null)
                    ? java.time.Duration.between(startedAt, endedAt).toMillis()
                    : null;
        }
    }
}
