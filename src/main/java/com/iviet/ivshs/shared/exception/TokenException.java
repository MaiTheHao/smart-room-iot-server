package com.iviet.ivshs.shared.exception;

public class TokenException extends UnauthorizedException {
    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
