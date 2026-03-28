package com.iviet.ivshs.exception.domain;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    protected BaseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    protected BaseException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.message = message;
    }
}
