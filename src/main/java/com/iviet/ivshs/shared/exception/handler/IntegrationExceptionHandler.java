package com.iviet.ivshs.shared.exception.handler;

import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.shared.exception.ExternalServiceException;
import com.iviet.ivshs.shared.exception.NetworkTimeoutException;
import com.iviet.ivshs.shared.exception.RemoteResourceNotFoundException;
import java.net.http.HttpConnectTimeoutException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(3)
@RestControllerAdvice(annotations = RestController.class)
public class IntegrationExceptionHandler {

        @ExceptionHandler({
                        HttpConnectTimeoutException.class,
                        NetworkTimeoutException.class
        })
        public ResponseEntity<ApiResponse<Void>> handleNetworkTimeout(Exception ex) {
                log.error("Network timeout", ex);
                String message = ex instanceof NetworkTimeoutException ? ex.getMessage() : "Network timeout while connecting to external device or service.";
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                                .body(ApiResponse.error(HttpStatus.GATEWAY_TIMEOUT, message));
        }

        @ExceptionHandler(ExternalServiceException.class)
        public ResponseEntity<ApiResponse<Void>> handleExternalCall(RuntimeException ex) {
                log.error("External service error", ex);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                .body(ApiResponse.error(HttpStatus.BAD_GATEWAY, ex.getMessage()));
        }

        @ExceptionHandler(RemoteResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleRemoteNotFound(RemoteResourceNotFoundException ex) {
                log.error("Remote resource not found", ex);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND, "Requested resource does not exist on the partner system."));
        }
}
