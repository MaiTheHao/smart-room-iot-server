package com.iviet.ivshs.shared.exception;

public class InvalidTokenException extends TokenException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
