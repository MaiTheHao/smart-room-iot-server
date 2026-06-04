package com.iviet.ivshs.config;

import java.util.List;
import java.util.TimeZone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
                "com.iviet.ivshs.controller.api", // REST controllers
                "com.iviet.ivshs.exception.handler", // ApiGlobalExceptionHandler + shared handlers
                "com.iviet.ivshs.aop" // RestRequestLoggingAspect + GlobalModelAttributes
}, excludeFilters = {
                // Loại trừ ViewRequestLoggingAspect khỏi API context
                @Filter(type = FilterType.REGEX, pattern = "com\\.iviet\\.ivshs\\.aop\\.ViewRequestLoggingAspect")
})
public class WebMvcApiConfig implements WebMvcConfigurer {

        @Bean
        @NonNull
        public ObjectMapper objectMapper() {
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
                return mapper;
        }

        @Override
        public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
                MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
                converter.setObjectMapper(objectMapper());
                converters.add(converter);
        }
}
