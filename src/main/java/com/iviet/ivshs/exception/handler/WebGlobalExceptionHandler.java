package com.iviet.ivshs.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

@ControllerAdvice()
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class WebGlobalExceptionHandler {

    private static final Logger log = LogManager.getLogger(WebGlobalExceptionHandler.class);
    private final LocaleResolver localeResolver;

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.contains("/api/");
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class, NotFoundException.class})
    public ModelAndView handleNotFound(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return null;
        }
        log.warn("Web 404 - Not Found: {}", request.getRequestURI());
        
        LocalContextUtil.setLocaleFromRequest(request, request.getSession(), localeResolver);
        
        ModelAndView mav = new ModelAndView("error/404.html");
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }

    @ExceptionHandler({AccessDeniedException.class, ForbiddenException.class})
    public ModelAndView handleAccessDenied(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return null;
        }
        log.warn("Web 403 - Access Denied: {}", request.getRequestURI());
        
        LocalContextUtil.setLocaleFromRequest(request, request.getSession(), localeResolver);
        
        ModelAndView mav = new ModelAndView("error/403.html");
        mav.setStatus(HttpStatus.FORBIDDEN);
        return mav;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ModelAndView handleUnauthorized(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return null;
        }
        log.warn("Web 401 - Unauthorized: {}", request.getRequestURI());
        
        LocalContextUtil.setLocaleFromRequest(request, request.getSession(), localeResolver);
        
        ModelAndView mav = new ModelAndView("redirect:/login");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralError(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return null;
        }
        log.error("Web 500 - System Error at {}: ", request.getRequestURI(), ex);
        
        LocalContextUtil.setLocaleFromRequest(request, request.getSession(), localeResolver);
        
        ModelAndView mav = new ModelAndView("error/500.html");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }
}