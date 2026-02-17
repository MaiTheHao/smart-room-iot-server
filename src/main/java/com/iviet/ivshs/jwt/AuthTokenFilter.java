package com.iviet.ivshs.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.iviet.ivshs.constant.AppConstant;

import io.jsonwebtoken.JwtException;

public class AuthTokenFilter extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION_HEADER = "Authorization";

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			if (!isNotRecursiveCall(request)) {
				String clientIp = getClientIp(request);
				logger.warn("RECURSIVE CALL DETECTED - URL: {}, Method: {}, IP: {}, User-Agent: {}", 
					request.getRequestURI(),
					request.getMethod(),
					clientIp,
					request.getHeader("User-Agent"));
				request.setAttribute("recursive_call", true);
				throw new JwtException("Recursive call detected");
			}

			String jwt = parseJwt(request);
			if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
				String username = jwtUtils.getUserNameFromJwtToken(jwt);
				authenticateUser(username, request);
			}
		} catch (JwtException e) {
			SecurityContextHolder.clearContext();
		} catch (UsernameNotFoundException e) {
			SecurityContextHolder.clearContext();
		} catch (Exception e) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private void authenticateUser(String username, HttpServletRequest request) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		logger.debug("User authenticated - Username: {}", username);
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(BEARER_PREFIX)) {
			return headerAuth.substring(BEARER_PREFIX.length());
		}
		return null;
	}

	private boolean isNotRecursiveCall(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		return userAgent == null || !userAgent.contains(AppConstant.APP_USER_AGENT);
	}

	private String getClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			return xForwardedFor.split(",")[0].trim();
		}
		String xRealIp = request.getHeader("X-Real-IP");
		if (xRealIp != null && !xRealIp.isEmpty()) {
			return xRealIp;
		}
		return request.getRemoteAddr();
	}
}
