package com.iviet.ivshs;

import java.util.EnumSet;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.iviet.ivshs.config.WebMvcApiConfig;
import com.iviet.ivshs.config.ApplicationConfig;
import com.iviet.ivshs.config.DataSourceConfig;
import com.iviet.ivshs.config.QuartzSchedulerConfig;
import com.iviet.ivshs.config.RestClientConfig;
import com.iviet.ivshs.config.WebSecurityConfig;
import com.iviet.ivshs.config.WebMvcViewConfig;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

public class SmrcApplication implements WebApplicationInitializer {

        @Override
        public void onStartup(ServletContext servletContext) throws ServletException {

                AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
                rootContext.register(
                                ApplicationConfig.class,
                                DataSourceConfig.class,
                                WebSecurityConfig.class,
                                QuartzSchedulerConfig.class,
                                RestClientConfig.class);

                servletContext.addListener(new ContextLoaderListener(rootContext));

                AnnotationConfigWebApplicationContext apiContext = new AnnotationConfigWebApplicationContext();
                apiContext.register(WebMvcApiConfig.class);
                apiContext.setParent(rootContext);

                DispatcherServlet apiDispatcher = new DispatcherServlet(apiContext);

                ServletRegistration.Dynamic apiReg = servletContext.addServlet("apiDispatcher", apiDispatcher);
                apiReg.setLoadOnStartup(1);
                apiReg.addMapping("/api/*");
                apiReg.setInitParameter("throwExceptionIfNoHandlerFound", "true");

                AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
                webContext.register(WebMvcViewConfig.class);
                webContext.setParent(rootContext);

                DispatcherServlet webDispatcher = new DispatcherServlet(webContext);

                ServletRegistration.Dynamic webReg = servletContext.addServlet("webDispatcher", webDispatcher);
                webReg.setLoadOnStartup(2);
                webReg.addMapping("/*");
                webReg.setInitParameter("throwExceptionIfNoHandlerFound", "true");
                webReg.setMultipartConfig(new MultipartConfigElement(
                                "",
                                5242880,
                                20971520,
                                0));

                CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
                encodingFilter.setEncoding("UTF-8");
                encodingFilter.setForceEncoding(true);
                FilterRegistration.Dynamic encodingReg = servletContext.addFilter("encodingFilter", encodingFilter);
                encodingReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

                FilterRegistration.Dynamic traceReg = servletContext.addFilter("requestTraceFilter",
                                new DelegatingFilterProxy("requestTraceFilter"));
                traceReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

                FilterRegistration.Dynamic rateLimitReg = servletContext.addFilter("rateLimitingFilter",
                                new DelegatingFilterProxy("rateLimitingFilter"));
                rateLimitReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

                FilterRegistration.Dynamic securityReg = servletContext.addFilter("springSecurityFilterChain",
                                new DelegatingFilterProxy("springSecurityFilterChain"));
                securityReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
        }
}
