package com.iviet.ivshs.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {
    
    @ModelAttribute("request")
    public HttpServletRequest addRequest(HttpServletRequest request) {
        return request;
    }
}
