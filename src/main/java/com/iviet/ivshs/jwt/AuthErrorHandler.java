package com.iviet.ivshs.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

@Component
public class AuthErrorHandler {

    public enum ErrorType {
        RECURSIVE_CALL("recursive_call"),
        TOKEN_EXPIRED("token_expired"),
        INVALID_SIGNATURE("invalid_signature"),
        MALFORMED_TOKEN("malformed_token"),
        INVALID_CREDENTIALS("invalid_credentials"),
        USER_NOT_FOUND("user_not_found"),
        INSUFFICIENT_AUTH("insufficient_auth"),
        UNKNOWN("unknown");

        private final String code;
        ErrorType(String code) { this.code = code; }
        public String getCode() { return code; }
    }

    public ErrorType determineErrorType(HttpServletRequest request, AuthenticationException exception) {
        if (request.getAttribute("recursive_call") != null) {
            return ErrorType.RECURSIVE_CALL;
        }

        Throwable cause = exception.getCause();
        if (cause instanceof ExpiredJwtException) {
            return ErrorType.TOKEN_EXPIRED;
        }
        if (cause instanceof SignatureException) {
            return ErrorType.INVALID_SIGNATURE;
        }
        if (cause instanceof JwtException && cause.getMessage().contains("JWT")) {
            return ErrorType.MALFORMED_TOKEN;
        }

        if (exception instanceof UsernameNotFoundException) {
            return ErrorType.USER_NOT_FOUND;
        }

        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("password") || message.contains("credential")) {
                return ErrorType.INVALID_CREDENTIALS;
            }
        }

        return ErrorType.INSUFFICIENT_AUTH;
    }

    public String getPublicMessage(ErrorType errorType) {
        return switch (errorType) {
            case RECURSIVE_CALL -> "Internal request processing failed - missing authentication token";
            case TOKEN_EXPIRED -> "Your session has expired. Please login again";
            case INVALID_SIGNATURE -> "Invalid authentication token";
            case MALFORMED_TOKEN -> "Malformed authentication token";
            case INVALID_CREDENTIALS -> "Invalid credentials provided";
            case USER_NOT_FOUND -> "Access denied";
            case INSUFFICIENT_AUTH -> "Authentication required to access this resource";
            case UNKNOWN -> "Authentication failed";
        };
    }

    public String getInternalMessage(ErrorType errorType, AuthenticationException exception) {
        return switch (errorType) {
            case RECURSIVE_CALL -> "Recursive call detected - internal request without JWT token: " + exception.getMessage();
            case TOKEN_EXPIRED -> "JWT token expired";
            case INVALID_SIGNATURE -> "JWT signature verification failed";
            case MALFORMED_TOKEN -> "JWT format invalid";
            case INVALID_CREDENTIALS -> "Bad credentials: " + exception.getMessage();
            case USER_NOT_FOUND -> "User not found: " + exception.getMessage();
            case INSUFFICIENT_AUTH -> "Authentication required: " + exception.getMessage();
            case UNKNOWN -> "Unknown auth error: " + exception.toString();
        };
    }

    public String getLogLevel(ErrorType errorType) {
        return switch (errorType) {
            case RECURSIVE_CALL -> "WARN";
            case INVALID_CREDENTIALS, TOKEN_EXPIRED -> "WARN";
            default -> "ERROR";
        };
    }
}
