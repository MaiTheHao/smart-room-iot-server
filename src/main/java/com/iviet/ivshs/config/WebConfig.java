package com.iviet.ivshs.config;

import java.util.List;
import java.util.Locale;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = { 
    "com.iviet.ivshs.controller", 
    "com.iviet.ivshs.exception",
    "com.iviet.ivshs.aop",
    "com.iviet.ivshs.web"
})
public class WebConfig implements WebMvcConfigurer, ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.of("vi", "VN"));
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    // ============ THYMELEAF VIEW RESOLVERS ============
    @Bean
    public ViewResolver htmlViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine(htmlTemplateResolver()));
        resolver.setContentType("text/html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setViewNames(new String[]{"*.html"});
        return resolver;
    }

    @Bean
    public ViewResolver javascriptViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine(javascriptTemplateResolver()));
        resolver.setContentType("application/javascript");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setViewNames(new String[]{"*.js"});
        return resolver;
    }

    @Bean
    public ViewResolver plainViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine(plainTemplateResolver()));
        resolver.setContentType("text/plain");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setViewNames(new String[]{"*.txt"});
        return resolver;
    }

    // ============ THYMELEAF TEMPLATE RESOLVERS ============
    private ISpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addDialect(new LayoutDialect());
        engine.addDialect(new SpringSecurityDialect());
        engine.setTemplateResolver(templateResolver);
        engine.setTemplateEngineMessageSource(messageSource());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    private ITemplateResolver htmlTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false);
        return resolver;
    }

    private ITemplateResolver javascriptTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("/WEB-INF/resources/scripts/");
        resolver.setTemplateMode(TemplateMode.JAVASCRIPT);
        resolver.setCacheable(false);
        return resolver;
    }

    private ITemplateResolver plainTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("/WEB-INF/resources/txt/");
        resolver.setTemplateMode(TemplateMode.TEXT);
        resolver.setCacheable(false);
        return resolver;
    }

    // ============ MULTIPART & HTTP CONVERTERS ============
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        converters.add(converter);
    }

    // ============ WEB MVC CONFIGURATION ============
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("/pages/login.html");
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/styles/**").addResourceLocations("/WEB-INF/resources/styles/").setCachePeriod(31556926);
        registry.addResourceHandler("/images/**").addResourceLocations("/WEB-INF/resources/img/").setCachePeriod(31556926);
        registry.addResourceHandler("/scripts/**").addResourceLocations("/WEB-INF/resources/scripts/").setCachePeriod(31556926);
        registry.addResourceHandler("/fonts/**").addResourceLocations("/WEB-INF/resources/fonts/").setCachePeriod(31556926);
        registry.addResourceHandler("/files/**").addResourceLocations("/WEB-INF/resources/file/").setCachePeriod(31556926);
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCachePeriod(31556926);
    }

    @Override
    public void configureDefaultServletHandling(@NonNull DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}