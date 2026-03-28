package com.iviet.ivshs.exception.domain;

import org.springframework.http.HttpStatus;

public class NetworkTimeoutException extends BaseException {
    public NetworkTimeoutException(String message) {
        super(HttpStatus.GATEWAY_TIMEOUT, message);
    }

    public NetworkTimeoutException(String message, Throwable cause) {
        super(HttpStatus.GATEWAY_TIMEOUT, message, cause);
    }
}
