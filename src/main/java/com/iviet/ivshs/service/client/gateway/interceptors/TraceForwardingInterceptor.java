package com.iviet.ivshs.service.client.gateway.interceptors;

import java.io.IOException;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class TraceForwardingInterceptor implements ClientHttpRequestInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SCENARIO_ID_HEADER = "X-Scenario-Id";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String traceId = ThreadContext.get("traceId");
        String scenarioId = ThreadContext.get("scenarioId");

        if (traceId != null && !traceId.isBlank()) {
            request.getHeaders().set(TRACE_ID_HEADER, traceId);
        }
        if (scenarioId != null && !scenarioId.isBlank()) {
            request.getHeaders().set(SCENARIO_ID_HEADER, scenarioId);
        }

        return execution.execute(request, body);
    }
}
