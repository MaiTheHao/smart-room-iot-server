package com.iviet.ivshs.exception.handler;

import com.iviet.ivshs.dto.ApiResponseV1;
import com.iviet.ivshs.exception.domain.ExternalServiceException;
import com.iviet.ivshs.exception.domain.NetworkTimeoutException;
import com.iviet.ivshs.exception.domain.RemoteResourceNotFoundException;

import java.net.http.HttpConnectTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(2)
@RestControllerAdvice(annotations = RestController.class)
public class IntegrationExceptionHandler {
    private static final Logger log = LogManager.getLogger(IntegrationExceptionHandler.class);

    @ExceptionHandler({HttpConnectTimeoutException.class, NetworkTimeoutException.class})
    public ResponseEntity<ApiResponseV1<Void>> handleNetworkTimeout(Exception ex) {
        log.error("Network timeout: {}", ex.getMessage());
        String message = ex instanceof NetworkTimeoutException ? ex.getMessage() : "Network timeout while connecting to external device or service.";
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ApiResponseV1.error(HttpStatus.GATEWAY_TIMEOUT, message));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponseV1<Void>> handleExternalCall(RuntimeException ex) {
        log.error("External service error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponseV1.error(HttpStatus.BAD_GATEWAY, ex.getMessage()));
    }

    @ExceptionHandler(RemoteResourceNotFoundException.class)
    public ResponseEntity<ApiResponseV1<Void>> handleRemoteNotFound(RemoteResourceNotFoundException ex) {
        log.error("Remote resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseV1.error(HttpStatus.NOT_FOUND, "Requested resource does not exist on the partner system."));
    }
}
