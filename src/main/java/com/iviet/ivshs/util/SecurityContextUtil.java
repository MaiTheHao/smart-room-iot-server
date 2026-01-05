package com.iviet.ivshs.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Slf4j
@UtilityClass
public class SecurityContextUtil {

    public Optional<String> getCurrentUsername() {
        return getAuthentication()
                .map(auth -> {
                    if (auth.getPrincipal() instanceof UserDetails userDetails) {
                        return userDetails.getUsername();
                    }
                    return auth.getPrincipal().toString();
                });
    }

    private Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated);
    }
}