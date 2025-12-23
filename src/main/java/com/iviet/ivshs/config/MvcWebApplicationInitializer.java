package com.iviet.ivshs.config;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;

public class MvcWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] { AppConfig.class };
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
	protected Filter[] getServletFilters() {
		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		encodingFilter.setForceEncoding(true);
		return new Filter[] { encodingFilter };
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
