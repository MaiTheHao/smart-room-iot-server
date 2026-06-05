package com.iviet.ivshs.integration.gateway.interceptor;

import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import com.iviet.ivshs.shared.logging.trace.TraceLogger;

@Component
@Slf4j
@RequiredArgsConstructor
public class TraceForwardingInterceptor implements ClientHttpRequestInterceptor {

    private final TraceLogger traceLogger;

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SCENARIO_ID_HEADER = "X-Scenario-Id";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String traceId = MDC.get("traceId");
        String scenarioId = MDC.get("scenarioId");

        if (traceId != null && !traceId.isBlank()) {
            request.getHeaders().set(TRACE_ID_HEADER, traceId);
        }
        if (scenarioId != null && !scenarioId.isBlank()) {
            request.getHeaders().set(SCENARIO_ID_HEADER, scenarioId);
        }

        Instant startedAt = Instant.now();
        int statusCode = -1;

        try {
            ClientHttpResponse response = execution.execute(request, body);
            statusCode = response.getStatusCode().value();
            return response;
        } finally {
            Instant endedAt = Instant.now();

            traceLogger.logTrace(
                    TraceLogger.TraceData.builder().traceId(traceId).scenarioId(scenarioId).type("GATEWAY_CLIENT").method(request.getMethod().name()).uri(request.getURI().toString()).status(statusCode).startedAt(startedAt).endedAt(endedAt).build());
        }
    }
}
