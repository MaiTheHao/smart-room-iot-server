package com.iviet.ivshs;

import java.util.EnumSet;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.iviet.ivshs.core.config.ApplicationConfig;
import com.iviet.ivshs.core.config.DataSourceConfig;
import com.iviet.ivshs.core.config.QuartzSchedulerConfig;
import com.iviet.ivshs.core.config.RestClientConfig;
import com.iviet.ivshs.core.config.WebMvcApiConfig;
import com.iviet.ivshs.core.config.WebMvcViewConfig;
import com.iviet.ivshs.core.config.WebSecurityConfig;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

public class SmrcApplication implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        // Cấu hình Root Context
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(ApplicationConfig.class, DataSourceConfig.class, WebSecurityConfig.class, QuartzSchedulerConfig.class, RestClientConfig.class);

        servletContext.addListener(new ContextLoaderListener(rootContext));

        // Cấu hình API Dispatcher Context
        AnnotationConfigWebApplicationContext apiContext = new AnnotationConfigWebApplicationContext();
        apiContext.register(WebMvcApiConfig.class);
        apiContext.setParent(rootContext);
        // Cấu hình API Dispatcher
        DispatcherServlet apiDispatcher = new DispatcherServlet(apiContext);
        ServletRegistration.Dynamic apiReg = servletContext.addServlet("apiDispatcher", apiDispatcher);
        apiReg.setLoadOnStartup(1);
        apiReg.addMapping("/api/*");
        apiReg.setInitParameter("throwExceptionIfNoHandlerFound", "true");

        // Cấu hình Web Dispatcher Context
        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.register(WebMvcViewConfig.class);
        webContext.setParent(rootContext);
        // Cấu hình Web Dispatcher
        DispatcherServlet webDispatcher = new DispatcherServlet(webContext);
        ServletRegistration.Dynamic webReg = servletContext.addServlet("webDispatcher", webDispatcher);
        webReg.setLoadOnStartup(2);
        webReg.addMapping("/*");
        webReg.setInitParameter("throwExceptionIfNoHandlerFound", "true");
        webReg.setMultipartConfig(new MultipartConfigElement("", 5242880, 20971520, 0));

        // Cấu hình Encoding Filter
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setForceEncoding(true);
        FilterRegistration.Dynamic encodingReg = servletContext.addFilter("encodingFilter", encodingFilter);
        encodingReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

        // Cấu hình Request Trace Filter
        FilterRegistration.Dynamic traceReg = servletContext.addFilter("requestTraceFilter", new DelegatingFilterProxy("requestTraceFilter"));
        traceReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

        // Cấu hình Security Filter
        FilterRegistration.Dynamic securityReg = servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy("springSecurityFilterChain"));
        securityReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

        // Cấu hình Rate Limiting Filter
        FilterRegistration.Dynamic rateLimitReg = servletContext.addFilter("rateLimitingFilter", new DelegatingFilterProxy("rateLimitingFilter"));
        rateLimitReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
    }
}
