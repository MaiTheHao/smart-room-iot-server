package com.iviet.ivshs.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.iviet.ivshs.dto.ApiResponseV1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Order(2)
public class GlobalApiExceptionHandler {
    
    private static final Logger log = LogManager.getLogger(GlobalApiExceptionHandler.class);



    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleNotFoundException(
            NotFoundException ex, WebRequest request) {
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleInternalServerErrorException(
            InternalServerErrorException ex, WebRequest request) {
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errorMessage.append(fieldError.getField())
                    .append(" - ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        });
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.BAD_REQUEST, errorMessage.toString());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleJsonProcessingException(
        JsonProcessingException ex, WebRequest request) {
        String errorMessage;
        if (ex instanceof JsonMappingException) {
            errorMessage = "Invalid data mapping: " + ex.getMessage();
        } else {
            errorMessage = "Invalid JSON format: " + ex.getMessage();
        }
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.BAD_REQUEST, errorMessage);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex, WebRequest request) {

        String errorMessage = "Invalid JSON request format.";
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof JsonProcessingException) {
            errorMessage = "Invalid JSON: " + ((JsonProcessingException) rootCause).getOriginalMessage();
        } else if (ex.getMessage() != null) {
            errorMessage = "Invalid request: " + ex.getMessage();
        }

        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.BAD_REQUEST, errorMessage);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.error("Authentication failed: ", ex);
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleUnsupportedOperationException(
        UnsupportedOperationException ex, WebRequest request) {
        log.error("Unsupported operation: ", ex);
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.BAD_REQUEST, "Operation not supported: " + ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleIllegalArgumentException(
        IllegalArgumentException ex, WebRequest request) {
        String errorMessage = "Invalid argument: " + ex.getMessage();
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.BAD_REQUEST, errorMessage);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException ex, WebRequest request) {
        String errorMessage = "Missing required parameter: " + ex.getParameterName();
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.BAD_REQUEST, errorMessage);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseV1<Object>> handleHttpRequestMethodNotSupportedException(
        org.springframework.web.HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String errorMessage = "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint. ";
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.METHOD_NOT_ALLOWED, errorMessage);
        return new ResponseEntity<>(apiResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseV1<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception occurred: ", ex);
        String message = "An unexpected system error has occurred. Please try again later.";
        ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.INTERNAL_SERVER_ERROR, message);
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
