package com.iviet.ivshs.exception.domain;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
	public UnauthorizedException(String message) {
		super(HttpStatus.UNAUTHORIZED, message);
	}

	public UnauthorizedException(String message, Throwable cause) {
		super(HttpStatus.UNAUTHORIZED, message, cause);
	}
}
