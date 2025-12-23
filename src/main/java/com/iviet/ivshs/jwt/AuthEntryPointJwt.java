package com.iviet.ivshs.jwt;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iviet.ivshs.dto.ApiResponseV1;
import org.springframework.http.HttpStatus;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

	private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
		logger.error("Unauthorized error: {}", authException.getMessage());

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		ApiResponseV1<Object> apiResponse = ApiResponseV1.error(HttpStatus.UNAUTHORIZED, authException.getMessage());
		
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.writeValue(response.getOutputStream(), apiResponse);
	}

}
