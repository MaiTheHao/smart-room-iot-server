package com.iviet.ivshs.config;

import java.util.Properties;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiPropertySource;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Import({ SecurityConfig.class })
@PropertySource("classpath:application.properties")
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScans(value = {
    @ComponentScan("com.iviet.ivshs.dao"),
    @ComponentScan("com.iviet.ivshs.mapper"),
    @ComponentScan("com.iviet.ivshs.service"),
    @ComponentScan("com.iviet.ivshs.repository"),
    @ComponentScan("com.iviet.ivshs.component")
})
public class AppConfig implements EnvironmentAware {

    private static final String JNDI_DATABASE_NAME = "java:comp/env/jdbc/smartroom_db";
    private static final String ENTITY_PACKAGES = "com.iviet.ivshs.entities";
    private static final String HIBERNATE_DIALECT = "org.hibernate.dialect.MySQLDialect";

    @Autowired
    private Environment env;

    @Override
    public void setEnvironment(@NonNull Environment env) {
        if (!(env instanceof ConfigurableEnvironment))  throw new IllegalArgumentException("Environment must be of type ConfigurableEnvironment");

        ConfigurableEnvironment configurableEnv = (ConfigurableEnvironment) env;
        
        try {
            configurableEnv.getPropertySources().addFirst(new JndiPropertySource("jndiPropertySource"));
            log.info("JNDI PropertySource added successfully. JNDI has priority.");
        } catch (Exception e) {
            log.warn("JNDI not available (running locally?). Fallback to application.properties.", e);
        }
    }

    @Bean
    public DataSource dataSource() {
        String jndiName = env.getProperty("jdbc.jndi-name", JNDI_DATABASE_NAME);
        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
        factoryBean.setJndiName(jndiName);
        factoryBean.setExpectedType(DataSource.class);
        factoryBean.setProxyInterface(DataSource.class);
        factoryBean.setLookupOnStartup(true);
        
        try {
            factoryBean.afterPropertiesSet();
            return (DataSource) factoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to lookup JNDI DataSource: " + jndiName, e);
        }
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan(ENTITY_PACKAGES);
        emf.setJpaVendorAdapter(createVendorAdapter());
        emf.setJpaProperties(createJpaProperties());
        return emf;
    }

    private HibernateJpaVendorAdapter createVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(env.getProperty("hibernate.show_sql", Boolean.class, false));
        adapter.setGenerateDdl(env.getProperty("hibernate.generate_ddl", Boolean.class, false));
        adapter.setDatabasePlatform(env.getProperty("hibernate.dialect", HIBERNATE_DIALECT));
        return adapter;
    }

    private Properties createJpaProperties() {
        Properties props = new Properties();
        props.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto", "validate"));
        props.put("hibernate.format_sql", env.getProperty("hibernate.format_sql", "true"));
        props.put("hibernate.connection.characterEncoding", "utf8");
        props.put("hibernate.connection.useUnicode", "true");
        return props;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
