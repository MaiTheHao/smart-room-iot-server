package com.iviet.ivshs.aop;

import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
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

@Aspect
@Component
public class RequestLoggingAspect {

  private static final Logger log = LogManager.getLogger(RequestLoggingAspect.class);
  private static final String LOG_TYPE_REST = "REST";
  private static final String LOG_TYPE_VIEW = "VIEW";

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
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    ThreadContext.put("requestId", requestId);
    ThreadContext.put("logType", logType);

    Instant start = Instant.now();
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes == null) {
      return joinPoint.proceed();
    }

    HttpServletRequest request = attributes.getRequest();
    String method = joinPoint.getSignature().toShortString();

    log.info(">>> [{}] START | ID: {} | Method: {} | URI: {} | Remote: {}",
        logType, requestId, request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

		log.debug("[{}] REQUEST DETAIL | Headers: {} | Params: {}", 
				logType, getHeadersInfo(request), request.getParameterMap());

    Object result = null;
    try {
      result = joinPoint.proceed();
    } catch (Throwable throwable) {
      log.error("[{}] FAILED | ID: {} | Error: {}", logType, requestId, throwable.getMessage());
      throw throwable;
    } finally {
      long duration = Duration.between(start, Instant.now()).toMillis();
      Integer status = getResponseStatus(attributes);

      log.info("<<< [{}] END | ID: {} | Status: {} | Duration: {}ms | Target: {}",
          logType, requestId, status, duration, method);

      if (log.isDebugEnabled()) {
        log.debug("[{}] RESPONSE DETAIL | Result: {}", logType, result);
      }
      ThreadContext.clearAll();
    }
    return result;
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