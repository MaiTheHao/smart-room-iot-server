package com.iviet.ivshs.aop;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yy - HH:mm:ss")
            .withZone(ZoneId.systemDefault());

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
        String requestId = UUID.randomUUID().toString();
        ThreadContext.put("requestId", requestId);
        ThreadContext.put("logType", logType);
        
        try {
            Instant startTime = Instant.now();
            
            if (RequestContextHolder.getRequestAttributes() == null) {
                return joinPoint.proceed();
            }
            
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            String formattedTime = TIME_FORMATTER.format(startTime);
            String prefix = "[" + logType + "]";

            log.info("\n========================= Start of {} Request ========================", logType);

            log.info(String.format(
                    "\n%s REQUEST\n" +
                    "* Class.Method : %s.%s\n" +
                    "* URL          : %s\n" +
                    "* HTTP Method  : %s\n" +
                    "* Start Time   : %s",
                    prefix,
                    className,
                    methodName,
                    request.getRequestURL(),
                    request.getMethod(),
                    formattedTime
            ));

            if (log.isDebugEnabled()) {
                log.debug(String.format(
                        "\n%s Request Details:\n" +
                        "* Headers : %s\n" +
                        "* Params  : %s",
                        prefix,
                        getHeadersInfo(request),
                        request.getParameterMap()
                ));
            }

            Object result = joinPoint.proceed();

            long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            Integer status = getResponseStatus();

            log.info(String.format(
                    "\n%s RESPONSE\n" +
                    "* Class.Method : %s.%s\n" +
                    "* Status       : %s\n" +
                    "* Duration     : %s ms",
                    prefix,
                    className,
                    methodName,
                    status != null ? status : "unknown",
                    duration
            ));

            if (log.isDebugEnabled()) {
                if (LOG_TYPE_REST.equals(logType)) {
                    log.debug("\n{} Response Body: {}", prefix, result);
                } else {
                    log.debug("\n{} Response (View Name / Model): {}", prefix, result);
                }
            }

            log.info("\n========================== End of {} Request =========================\n", logType);

            return result;
        } finally {
            ThreadContext.clearAll();
        }
    }

    private Integer getResponseStatus() {
        try {
            if (RequestContextHolder.getRequestAttributes() == null) {
                return null;
            }
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
            return (response != null) ? response.getStatus() : null;
        } catch (Exception e) {
            log.warn("Can't get HTTP response status. {}", e.getMessage());
            return null;
        }
    }
    
    private Map<String, String> getHeadersInfo(HttpServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }
}
