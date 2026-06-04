package com.iviet.ivshs.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.iviet.ivshs.exception.domain.ForbiddenException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.exception.domain.UnauthorizedException;
import com.iviet.ivshs.util.LocalContextUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class WebGlobalExceptionHandler {

    private final LocaleResolver localeResolver;

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class, NotFoundException.class})
    public ModelAndView handleNotFound(Exception ex, HttpServletRequest request) {
        log.warn("Web resource not found: uri={}", request.getRequestURI());
        LocalContextUtil.setLocaleFromRequest(request, request.getSession(), localeResolver);
        ModelAndView mav = new ModelAndView("error/404.html");
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }

    @ExceptionHandler({AccessDeniedException.class, ForbiddenException.class})
    public ModelAndView handleAccessDenied(Exception ex, HttpServletRequest request) {
        log.warn("Web access denied: uri={}", request.getRequestURI());
        LocalContextUtil.setLocaleFromRequest(request, request.getSession(), localeResolver);
        ModelAndView mav = new ModelAndView("error/403.html");
        mav.setStatus(HttpStatus.FORBIDDEN);
        return mav;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ModelAndView handleUnauthorized(Exception ex, HttpServletRequest request) {
        log.warn("Web unauthorized access: uri={}", request.getRequestURI());
        LocalContextUtil.setLocaleFromRequest(request, request.getSession(), localeResolver);
        ModelAndView mav = new ModelAndView("error/401.html");
        mav.setStatus(HttpStatus.UNAUTHORIZED);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralError(Exception ex, HttpServletRequest request) {
        log.error("Web system error occurred: uri={}", request.getRequestURI(), ex);
        LocalContextUtil.setLocaleFromRequest(request, request.getSession(), localeResolver);
        ModelAndView mav = new ModelAndView("error/500.html");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }
}