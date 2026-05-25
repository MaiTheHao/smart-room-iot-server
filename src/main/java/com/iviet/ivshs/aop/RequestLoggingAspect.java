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

import org.slf4j.MDC;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.iviet.ivshs.apm.TraceLogger;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestLoggingAspect {
  private static final String LOG_TYPE_REST = "REST";
  private static final String LOG_TYPE_VIEW = "VIEW";
  private static final int MAX_LOG_LENGTH = 500;

  private final TraceLogger traceLogger;

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
    String traceId = MDC.get("traceId");
    if (traceId == null) {
      traceId = UUID.randomUUID().toString();
      MDC.put("traceId", traceId);
    }
    MDC.put("logType", logType);

    String startedAtStr = MDC.get("startedAt");
    Instant start = startedAtStr != null ? Instant.parse(startedAtStr) : Instant.now();
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes == null) {
      return joinPoint.proceed();
    }

    HttpServletRequest request = attributes.getRequest();
    String controllerMethod = joinPoint.getSignature().toShortString();

    log.info("Request started: type={}, id={}, method={}, uri={}, remote={}",
        logType, traceId, request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

    log.debug("Request details: type={}, headers={}, params={}",
        logType, getHeadersInfo(request), request.getParameterMap());

    Object result = null;
    try {
      result = joinPoint.proceed();
    } catch (Throwable throwable) {
      log.error("Request failed: type={}, id={}", logType, traceId, throwable);
      throw throwable;
    } finally {
      Instant endedAt = Instant.now();
      long duration = Duration.between(start, endedAt).toMillis();
      Integer status = getResponseStatus(attributes);

      log.info("Request completed: type={}, id={}, status={}, duration={}ms, target={}",
          logType, traceId, status, duration, controllerMethod);

      if (log.isDebugEnabled()) {
        log.debug("Response details: type={}, result={}", logType, truncateResult(result));
      }

      traceLogger.logTrace(TraceLogger.TraceData.builder()
          .traceId(traceId)
          .scenarioId(MDC.get("scenarioId"))
          .type(logType)
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
    if (result == null)
      return "null";
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