package com.iviet.ivshs.config;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springframework.web.filter.DelegatingFilterProxy;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import java.util.EnumSet;

public class MvcWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] { AppConfig.class, RestConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] { WebConfig.class };
	}

	@Override
	@NonNull
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	@Override
	public void onStartup(@NonNull ServletContext servletContext) throws ServletException {
		// 1. Call super first to let Spring initialize the contexts and
		// DispatcherServlet
		super.onStartup(servletContext);

		// 2. Character Encoding: Đảm bảo luồng dữ liệu có thể hiểu UTF-8
		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		encodingFilter.setForceEncoding(true);
		FilterRegistration.Dynamic encodingReg = servletContext.addFilter("encodingFilter", encodingFilter);
		encodingReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

		// 3. Request Trace Filter: Truy vết req/res cho debugging và monitoring
		FilterRegistration.Dynamic traceReg = servletContext.addFilter("requestTraceFilter",
				new DelegatingFilterProxy("requestTraceFilter"));
		traceReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

		// 4. Rate Limiting Filter: Giới hạn tần suất req để bảo vệ server
		FilterRegistration.Dynamic rateLimitReg = servletContext.addFilter("rateLimitingFilter",
				new DelegatingFilterProxy("rateLimitingFilter"));
		rateLimitReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

		// 5. Spring Security Filter Chain: Sử dụng Spring Security để bảo vệ các
		// endpoint
		FilterRegistration.Dynamic securityReg = servletContext.addFilter("springSecurityFilterChain",
				new DelegatingFilterProxy("springSecurityFilterChain"));
		securityReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
	}

	@Override
	protected void customizeRegistration(@NonNull ServletRegistration.Dynamic registration) {
		// Cấu hình multipart (file upload)
		registration.setMultipartConfig(new MultipartConfigElement(
				"", // location - temp directory
				5242880, // maxFileSize - 5MB
				20971520, // maxRequestSize - 20MB
				0 // fileSizeThreshold
		));

		// Bật exception handling để có thể forward đến error pages
		registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");
	}
}
