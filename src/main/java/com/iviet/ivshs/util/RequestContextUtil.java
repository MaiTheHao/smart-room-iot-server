package com.iviet.ivshs.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@UtilityClass
public class RequestContextUtil {

    public boolean isHttpRequest() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes;
    }

    public HttpServletRequest getCurrentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
	
    public boolean isInternalCall() {
        return !isHttpRequest();
    }
}