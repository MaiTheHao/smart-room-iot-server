package com.iviet.ivshs.aop;

import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.iviet.ivshs.apm.TraceLogger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ViewRequestLoggingAspect {

    private static final String LOG_TYPE = "VIEW";
    private static final int MAX_LOG_LENGTH = 500;

    private final TraceLogger traceLogger;

    @Around("within(com.iviet.ivshs.controller.view..*)")
    public Object logAroundView(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
        }
        MDC.put("logType", LOG_TYPE);

        String startedAtStr = MDC.get("startedAt");
        Instant start = startedAtStr != null ? Instant.parse(startedAtStr) : Instant.now();
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String controllerMethod = joinPoint.getSignature().toShortString();

        log.info("Request started: type={}, id={}, method={}, uri={}, remote={}",
                LOG_TYPE, traceId, request.getMethod(), request.getRequestURI(),
                request.getRemoteAddr());
        log.debug("Request details: type={}, headers={}, params={}",
                LOG_TYPE, getHeadersInfo(request), request.getParameterMap());

        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("Request failed: type={}, id={}", LOG_TYPE, traceId, throwable);
            throw throwable;
        } finally {
            Instant endedAt = Instant.now();
            long duration = Duration.between(start, endedAt).toMillis();
            Integer status = getResponseStatus(attributes);

            log.info("Request completed: type={}, id={}, status={}, duration={}ms, target={}",
                    LOG_TYPE, traceId, status, duration, controllerMethod);

            if (log.isDebugEnabled()) {
                log.debug("Response details: type={}, result={}", LOG_TYPE, truncateResult(result));
            }

            traceLogger.logTrace(TraceLogger.TraceData.builder()
                    .traceId(traceId)
                    .scenarioId(MDC.get("scenarioId"))
                    .type(LOG_TYPE)
                    .method(request.getMethod())
                    .uri(request.getRequestURI())
                    .status(status)
                    .controller(controllerMethod)
                    .remote(request.getRemoteAddr())
                    .startedAt(start)
                    .endedAt(endedAt)
                    .build());

            MDC.remove("logType");
        }
        return result;
    }

    private String truncateResult(Object result) {
        if (result == null) return "null";
        String strResult = result.toString();
        return strResult.length() > MAX_LOG_LENGTH
                ? strResult.substring(0, MAX_LOG_LENGTH) + "... (truncated, total: " + strResult.length() + ")"
                : strResult;
    }

    private Integer getResponseStatus(ServletRequestAttributes attributes) {
        HttpServletResponse response = attributes.getResponse();
        return response != null ? response.getStatus() : null;
    }

    private Map<String, String> getHeadersInfo(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }
}
