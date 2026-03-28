package com.iviet.ivshs.jwt;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.jwt.AuthErrorHandler.ErrorType;
import org.springframework.http.HttpStatus;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthErrorHandler authErrorHandler;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ErrorType errorType = authErrorHandler.determineErrorType(request, authException);
        String internalMessage = authErrorHandler.getInternalMessage(errorType, authException);
        String clientIp = getClientIp(request);

        if ("WARN".equals(authErrorHandler.getLogLevel(errorType))) {
            logger.warn("[{}] Authentication failed - URL: {}, Method: {}, IP: {}, Exception: {} | {}",
                errorType.getCode(),
                request.getRequestURI(),
                request.getMethod(),
                clientIp,
                authException.getClass().getSimpleName(),
                internalMessage);
        } else {
            logger.error("[{}] Authentication failed - URL: {}, Method: {}, IP: {}, Exception: {} | {}",
                errorType.getCode(),
                request.getRequestURI(),
                request.getMethod(),
                clientIp,
                authException.getClass().getSimpleName(),
                internalMessage);
        }

        String publicMessage = authErrorHandler.getPublicMessage(errorType);
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Object> apiResponse = ApiResponse.error(HttpStatus.UNAUTHORIZED, publicMessage);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

}
