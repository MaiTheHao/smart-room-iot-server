package com.iviet.ivshs.core.config;

import java.util.TimeZone;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jndi.JndiPropertySource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Slf4j
@Configuration("appConfig")
@PropertySource("classpath:application.properties")
@PropertySource(value = "classpath:application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableAspectJAutoProxy
@RequiredArgsConstructor
@ComponentScan(basePackages = "com.iviet.ivshs", excludeFilters = {
        @Filter(type = FilterType.ANNOTATION, classes = {
                Controller.class,
                RestController.class,
                ControllerAdvice.class
        }),
        @Filter(type = FilterType.REGEX, pattern = "com\\.iviet\\.ivshs\\.core\\.config\\..*")
})
public class ApplicationConfig implements EnvironmentAware {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void setEnvironment(@NonNull
    Environment env) {
        if (!(env instanceof ConfigurableEnvironment configurableEnv)) {
            return;
        }
        String profile = env.getProperty("spring.profiles.active", "dev");
        if ("prod".equals(profile)) {
            try {
                configurableEnv.getPropertySources()
                        .addFirst(new JndiPropertySource("jndiPropertySource"));
                log.info("JNDI PropertySource added successfully.");
            } catch (Exception e) {
                log.error("JNDI PropertySource failed in prod — check server JNDI config!", e);
            }
        } else {
            log.debug("JNDI skipped for profile='{}'", profile);
        }
    }

    // ============ ENVIRONMENT ============
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    // ============ MESSAGE & LOCALE ============
    @Bean
    @Description("Spring Message Resolver for i18n")
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    @Lazy
    public Validator validator() {
        return new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        return mapper;
    }
}
