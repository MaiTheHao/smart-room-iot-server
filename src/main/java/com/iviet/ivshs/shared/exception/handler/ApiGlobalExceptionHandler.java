package com.iviet.ivshs.shared.exception.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.BaseException;
import com.iviet.ivshs.shared.exception.ForbiddenException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Order(2)
@RestControllerAdvice(basePackages = "com.iviet.ivshs.controller.api")
public class ApiGlobalExceptionHandler {

  // ====== SPRING CONTROLLER AUTO-THROWN EXCEPTIONS ======

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    Class<?> requiredType = ex.getRequiredType();
    String msg;

    if (requiredType != null && requiredType.isEnum()) {
      String validValues = Arrays.stream(requiredType.getEnumConstants())
          .map(Object::toString)
          .collect(Collectors.joining(", "));
      msg = String.format(
          "Invalid value '%s' for parameter '%s'. Accepted values are: [%s].",
          ex.getValue(), ex.getName(), validValues);
    } else {
      msg = String.format(
          "Invalid value '%s' for parameter '%s'. Expected type: %s.",
          ex.getValue(),
          ex.getName(),
          requiredType != null ? requiredType.getSimpleName() : "unknown");
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, msg));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParams(
      MissingServletRequestParameterException ex) {
    String msg = "Required parameter is missing: " + ex.getParameterName();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, msg));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    String msg = buildValidationMessage(ex);
    log.warn("Request validation failed: {}", msg);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, msg));
  }

  @ExceptionHandler(UnrecognizedPropertyException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnrecognizedProperty(
      UnrecognizedPropertyException ex) {
    String msg = String.format(
        "Property '%s' is unrecognized. Please check your request payload.", ex.getPropertyName());
    log.warn("Unrecognized property: property={}", ex.getPropertyName());
    log.debug("Jackson UnrecognizedPropertyException details", ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, msg));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    String msg =
        String.format("HTTP method '%s' is not supported for this request.", ex.getMethod());
    log.warn("HTTP method not supported: method={}", ex.getMethod());
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED, msg));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    String msg = "Malformed JSON request or invalid data format.";
    Throwable cause = ex.getCause();

    log.debug("HttpMessageNotReadableException details", ex);

    if (cause instanceof JsonMappingException jme) {
      StringBuilder pathBuilder = new StringBuilder();
      for (JsonMappingException.Reference ref : jme.getPath()) {
        if (ref.getFieldName() != null) {
          if (!pathBuilder.isEmpty()) pathBuilder.append(".");
          pathBuilder.append(ref.getFieldName());
        } else if (ref.getIndex() >= 0) {
          pathBuilder.append("[").append(ref.getIndex()).append("]");
        }
      }
      String fieldName = !pathBuilder.isEmpty() ? pathBuilder.toString() : "unknown";

      if (jme instanceof InvalidFormatException ife) {
        if (ife.getTargetType() != null) {
          if (ife.getTargetType().isEnum()) {
            String validValues = Arrays.stream(ife.getTargetType().getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
            msg = String.format(
                "Invalid value '%s' for field '%s'. Accepted values are: [%s]",
                ife.getValue(), fieldName, validValues);
          } else {
            msg = String.format(
                "Invalid value '%s' for field '%s'. Expected type: %s",
                ife.getValue(), fieldName, ife.getTargetType().getSimpleName());
          }
        }
      } else if (jme instanceof ValueInstantiationException vie) {
        Class<?> targetType = vie.getType() != null ? vie.getType().getRawClass() : null;

        if (targetType != null && targetType.isEnum()) {
          String validValues = Arrays.stream(targetType.getEnumConstants())
              .map(Object::toString)
              .collect(Collectors.joining(", "));

          String detailMsg = vie.getCause() != null ? vie.getCause().getMessage() : "Invalid input";
          msg = String.format(
              "Invalid value for field '%s'. Accepted values are: [%s]. (%s)",
              fieldName, validValues, detailMsg);
        } else if (vie.getCause() != null) {
          msg = vie.getCause().getMessage();
        } else {
          String typeName = targetType != null ? targetType.getSimpleName() : "unknown type";
          msg = String.format(
              "Invalid format for field '%s'. Expected type: %s, but got: %s",
              fieldName, typeName, vie.getMessage());
        }
      }
    } else if (cause instanceof JsonParseException) {
      msg = "Malformed JSON request. Please check the JSON syntax (e.g. missing quotes, trailing"
          + " commas).";
    } else if (cause != null && cause.getMessage() != null) {
      msg = cause.getMessage().split("\n")[0];
    }

    log.warn("Bad Request (HttpMessageNotReadable): msg={}", msg);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, msg));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    String msg = ex.getConstraintViolations().iterator().next().getMessage();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, msg));
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(
      HandlerMethodValidationException ex) {
    String msg = buildValidationMessage(ex);
    log.warn("Request validation failed: {}", msg);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, msg));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex) {
    HttpStatus status = Optional.ofNullable(
            HttpStatus.resolve(ex.getStatusCode().value()))
        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    String msg = Optional.ofNullable(ex.getReason()).orElseGet(status::getReasonPhrase);
    return ResponseEntity.status(status).body(ApiResponse.error(status, msg));
  }

  private String buildValidationMessage(MethodArgumentNotValidException ex) {
    List<String> details = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> String.format("%s: %s", fe.getField(), fe.getDefaultMessage()))
        .toList();

    if (!details.isEmpty()) {
      return "Validation failed: " + String.join(", ", details);
    }
    return ex.getBindingResult().getAllErrors().stream()
        .findFirst()
        .map(MessageSourceResolvable::getDefaultMessage)
        .orElse("Validation failure");
  }

  private String buildValidationMessage(HandlerMethodValidationException ex) {
    List<String> details = ex.getParameterValidationResults().stream()
        .flatMap(this::formatParameterValidationResult)
        .toList();

    return details.isEmpty()
        ? "Validation failure"
        : "Validation failed: " + String.join(", ", details);
  }

  private Stream<String> formatParameterValidationResult(ParameterValidationResult result) {
    String param = result.getMethodParameter().getParameterName();
    Integer idx = result.getContainerIndex();
    String prefix = (idx != null) ? String.format("%s[%d]", param, idx) : param;

    return result.getResolvableErrors().stream().map(err -> formatResolvableError(prefix, err));
  }

  private String formatResolvableError(String prefix, MessageSourceResolvable err) {
    String field = (err instanceof FieldError fe) ? fe.getField() : null;
    String target = (field != null) ? prefix + "." + field : prefix;
    return String.format("%s: %s", target, err.getDefaultMessage());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex) {
    String msg = "Content-Type '" + ex.getContentType() + "' is not supported.";
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(ApiResponse.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, msg));
  }

  // ====== SECURITY & AUTHENTICATION EXCEPTIONS ======

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
      AuthenticationException ex) {
    log.warn("Authentication failed: message={}", ex.getMessage());
    log.debug("Authentication failure details", ex);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(
            HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage()));
  }

  @ExceptionHandler({
    org.springframework.security.access.AccessDeniedException.class,
    org.springframework.security.authorization.AuthorizationDeniedException.class
  })
  public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(Exception ex) {
    log.warn("Access denied: message={}", ex.getMessage());
    log.debug("Access denied details", ex);
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage()));
  }

  // ====== CUSTOM DOMAIN EXCEPTIONS ======

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(HttpStatus.UNAUTHORIZED, ex.getMessage()));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiResponse<Void>> handleForbiddenException(ForbiddenException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error(HttpStatus.FORBIDDEN, ex.getMessage()));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
    HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status).body(ApiResponse.error(status, ex.getMessage()));
  }

  // ====== OTHER EXCEPTIONS ======

  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<ApiResponse<Object>> handleUnsupportedOperationException(
      UnsupportedOperationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(
            HttpStatus.BAD_REQUEST, "Operation not supported: " + ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: message={}", ex.getMostSpecificCause().getMessage());
    log.debug("Data integrity details", ex);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error(
            HttpStatus.CONFLICT,
            "Resource conflict: a record with the same unique identifier already exists."));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
    log.error("Unexpected system error occurred", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error."));
  }
}
