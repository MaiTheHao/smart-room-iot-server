package com.iviet.ivshs.shared.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidClientTypeException extends AuthenticationException {
    public InvalidClientTypeException(String msg) {
        super(msg);
    }
}
