package com.iviet.ivshs.shared.exception;

public class TokenExpiredException extends TokenException {
    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
