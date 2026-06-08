package com.iviet.ivshs.core.config;

import java.util.List;
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

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
    "com.iviet.ivshs.controller.api",
    "com.iviet.ivshs.shared.exception.handler"
}, excludeFilters = {
    @Filter(type = FilterType.REGEX, pattern = "com\\.iviet\\.ivshs\\.shared\\.logging\\.ViewRequestLoggingAspect")
})
public class WebMvcApiConfig implements WebMvcConfigurer {

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void configureMessageConverters(@NonNull
  List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
    converters.add(converter);
  }
}
