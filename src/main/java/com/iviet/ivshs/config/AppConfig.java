package com.iviet.ivshs.config;

import java.util.Properties;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiPropertySource;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Import({ SecurityConfig.class, WebConfig.class, CacheConfig.class, QuartzConfig.class })
@PropertySource("classpath:application.properties")
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScans(value = {
    @ComponentScan("com.iviet.ivshs.dao"),
    @ComponentScan("com.iviet.ivshs.engine"),
    @ComponentScan("com.iviet.ivshs.service"),
    @ComponentScan("com.iviet.ivshs.repository"),
    @ComponentScan("com.iviet.ivshs.component"),
    @ComponentScan("com.iviet.ivshs.startup"),
    @ComponentScan("com.iviet.ivshs.util"),
    @ComponentScan("com.iviet.ivshs.schedule"),
    @ComponentScan("com.iviet.ivshs.rule"),
})
public class AppConfig implements EnvironmentAware {

    private static final String DEFAULT_JNDI_NAME = "java:comp/env/jdbc/smartroom_db";
    private static final String ENTITY_PACKAGES = "com.iviet.ivshs.entities";

    @Autowired
    private Environment env;

    @Override
    public void setEnvironment(@NonNull Environment env) {
        if (!(env instanceof ConfigurableEnvironment)) {
            return;
        }
        ConfigurableEnvironment configurableEnv = (ConfigurableEnvironment) env;
        try {
            configurableEnv.getPropertySources().addFirst(new JndiPropertySource("jndiPropertySource"));
            log.info("JNDI PropertySource added successfully.");
        } catch (Exception e) {
            log.warn("JNDI not available. Application will use local properties.");
        }
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
    @NonNull
    public DataSource dataSource() {
        String jndiName = env.getProperty("jdbc.jndi-name", DEFAULT_JNDI_NAME);
        
        try {
            JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
            factoryBean.setJndiName(jndiName);
            factoryBean.setExpectedType(DataSource.class);
            factoryBean.setProxyInterface(DataSource.class);
            factoryBean.setLookupOnStartup(true);
            factoryBean.afterPropertiesSet();
            
            log.info("Connected via JNDI: {}", jndiName);
            DataSource ds = (DataSource) factoryBean.getObject();
            if (ds == null) throw new IllegalArgumentException("DataSource from JNDI is null");
            
            return ds;
        } catch (Exception e) {
            log.warn("JNDI lookup failed ({}). Switching to Local JDBC Driver.", e.getMessage());
            
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(env.getRequiredProperty("jdbc.driverClassName"));
            dataSource.setUrl(env.getRequiredProperty("jdbc.url"));
            dataSource.setUsername(env.getRequiredProperty("jdbc.username"));
            dataSource.setPassword(env.getRequiredProperty("jdbc.password"));
            
            log.info("Connected via Local JDBC Driver: {}", env.getProperty("jdbc.url"));
            return dataSource;
        }
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan(ENTITY_PACKAGES);
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);
        emf.setJpaProperties(createJpaProperties());
        
        return emf;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@NonNull DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @NonNull
    private Properties createJpaProperties() {
        Properties props = new Properties();
        
        props.put("hibernate.dialect", env.getProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"));
        props.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto", "validate"));
        props.put("hibernate.show_sql", env.getProperty("hibernate.show_sql", "false"));
        props.put("hibernate.format_sql", env.getProperty("hibernate.format_sql", "false"));
        
        props.put("hibernate.jdbc.batch_size", env.getProperty("hibernate.jdbc.batch_size", "50"));
        props.put("hibernate.order_inserts", env.getProperty("hibernate.order_inserts", "true"));
        props.put("hibernate.order_updates", env.getProperty("hibernate.order_updates", "true"));

        props.put("hibernate.connection.characterEncoding", "utf8");
        props.put("hibernate.connection.useUnicode", "true");
        props.put("hibernate.connection.charSet", "UTF-8");

        return props;
    }

    @Bean
    public PlatformTransactionManager transactionManager(@NonNull EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}