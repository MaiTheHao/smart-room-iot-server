package com.iviet.ivshs.exception.domain;

import org.springframework.http.HttpStatus;

public class RemoteResourceNotFoundException extends BaseException {
    public RemoteResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public RemoteResourceNotFoundException(String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, message, cause);
    }
}
