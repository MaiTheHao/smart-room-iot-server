package com.iviet.ivshs.aop;

import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class RequestLoggingAspect {

  private static final Logger log = LogManager.getLogger(RequestLoggingAspect.class);
  private static final String LOG_TYPE_REST = "REST";
  private static final String LOG_TYPE_VIEW = "VIEW";
  private static final int MAX_LOG_LENGTH = 500;

  private final ObjectMapper objectMapper;

  @Pointcut("within(com.iviet.ivshs.controller.api..*)")
  public void restfulControllerMethods() {
  }

  @Pointcut("within(com.iviet.ivshs.controller.view..*)")
  public void viewControllerMethods() {
  }

  @Around("restfulControllerMethods()")
  public Object logAroundRest(ProceedingJoinPoint joinPoint) throws Throwable {
    return logRequestAndProceed(joinPoint, LOG_TYPE_REST);
  }

  @Around("viewControllerMethods()")
  public Object logAroundView(ProceedingJoinPoint joinPoint) throws Throwable {
    return logRequestAndProceed(joinPoint, LOG_TYPE_VIEW);
  }

  private Object logRequestAndProceed(ProceedingJoinPoint joinPoint, String logType) throws Throwable {
    String traceId = ThreadContext.get("traceId");
    if (traceId == null) {
      traceId = UUID.randomUUID().toString();
      ThreadContext.put("traceId", traceId);
    }
    ThreadContext.put("logType", logType);

    Instant start = Instant.now();
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes == null) {
      return joinPoint.proceed();
    }

    HttpServletRequest request = attributes.getRequest();
    String controllerMethod = joinPoint.getSignature().toShortString();

    log.info(">>> [{}] START | ID: {} | Method: {} | URI: {} | Remote: {}",
        logType, traceId, request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

    log.debug("[{}] REQUEST DETAIL | Headers: {} | Params: {}", 
        logType, getHeadersInfo(request), request.getParameterMap());

    Object result = null;
    try {
      result = joinPoint.proceed();
    } catch (Throwable throwable) {
      log.error("[{}] FAILED | ID: {} | Error: {}", logType, traceId, throwable.getMessage());
      throw throwable;
    } finally {
      long duration = Duration.between(start, Instant.now()).toMillis();
      Integer status = getResponseStatus(attributes);

      log.info("<<< [{}] END | ID: {} | Status: {} | Duration: {}ms | Target: {}",
          logType, traceId, status, duration, controllerMethod);

      if (log.isDebugEnabled()) {
        log.debug("[{}] RESPONSE DETAIL | Result: {}", logType, truncateResult(result));
      }

      // APM compact JSON — chỉ ghi khi TRACE được bật (APM_LOG_LEVEL=trace)
      final String capturedTraceId = traceId;
      final String capturedScenario = ThreadContext.get("scenarioId");
      final String capturedMethod = request.getMethod();
      final String capturedUri = request.getRequestURI();
      final String capturedRemote = request.getRemoteAddr();
      final Integer capturedStatus = status;
      final long capturedDuration = duration;
      final String capturedController = controllerMethod;
      final String capturedType = logType;

      log.trace(() -> {
        try {
          Map<String, Object> m = new LinkedHashMap<>();
          m.put("traceId", capturedTraceId);
          m.put("scenarioId", capturedScenario != null ? capturedScenario : "");
          m.put("type", capturedType);
          m.put("method", capturedMethod);
          m.put("uri", capturedUri);
          m.put("status", capturedStatus);
          m.put("duration_ms", capturedDuration);
          m.put("controller", capturedController);
          m.put("remote", capturedRemote);
          return objectMapper.writeValueAsString(m);
        } catch (Exception e) {
          return "{}";
        }
      });

      ThreadContext.remove("logType");
    }
    return result;
  }

  private String truncateResult(Object result) {
    if (result == null) return "null";
    String strResult = result.toString();
    if (strResult.length() > MAX_LOG_LENGTH) {
      return strResult.substring(0, MAX_LOG_LENGTH) + "... (truncated, total length: " + strResult.length() + ")";
    }
    return strResult;
  }

  private Integer getResponseStatus(ServletRequestAttributes attributes) {
    HttpServletResponse response = attributes.getResponse();
    return (response != null) ? response.getStatus() : null;
  }

  private Map<String, String> getHeadersInfo(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames != null && headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      headers.put(name, request.getHeader(name));
    }
    return headers;
  }
}