package com.iviet.ivshs.exception.domain;

import org.springframework.security.core.AuthenticationException;

public class InvalidClientTypeException extends AuthenticationException {
    public InvalidClientTypeException(String msg) {
        super(msg);
    }
}
