package com.iviet.ivshs.exception.domain;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
	public ForbiddenException(String message) {
		super(HttpStatus.FORBIDDEN, message);
	}

	public ForbiddenException(String message, Throwable cause) {
		super(HttpStatus.FORBIDDEN, message, cause);
	}
}
