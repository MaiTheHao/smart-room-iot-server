package com.iviet.ivshs.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(1)
public class GlobalWebExceptionHandler {

	private static final Logger log = LogManager.getLogger(GlobalWebExceptionHandler.class);

	private boolean isApiRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri != null && uri.contains("/api/");
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ModelAndView handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
		if (isApiRequest(request)) {
			return null;
		}
		log.warn("Page not found: {}", ex.getRequestURL());
		ModelAndView mav = new ModelAndView("error/404.html");
		mav.setStatus(HttpStatus.NOT_FOUND);
		return mav;
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ModelAndView handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
		if (isApiRequest(request)) {
			return null;
		}
		log.warn("Resource not found: {}", ex.getResourcePath());
		ModelAndView mav = new ModelAndView("error/404.html");
		mav.setStatus(HttpStatus.NOT_FOUND);
		return mav;
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
		if (isApiRequest(request)) {
			throw ex;
		}
		log.warn("Access denied for user: {} to {}", request.getRemoteUser(), request.getRequestURI());
		return "error/401.html";
	}
}
