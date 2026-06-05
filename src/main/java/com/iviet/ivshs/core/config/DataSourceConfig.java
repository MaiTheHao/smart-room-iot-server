package com.iviet.ivshs.core.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.iviet.ivshs.core.properties.DatabaseProperties;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class DataSourceConfig {

    private static final String DEFAULT_JNDI_NAME = "java:comp/env/jdbc/smartroom_db";
    private static final String ENTITY_PACKAGES = "com.iviet.ivshs.entities";

    private final DatabaseProperties dbProps;

    @Bean
    @NonNull
    public DataSource dataSource() {
        String jndiName = dbProps.getJndiName();
        if (jndiName == null || jndiName.trim().isEmpty()) {
            jndiName = DEFAULT_JNDI_NAME;
        }

        try {
            JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
            factoryBean.setJndiName(jndiName);
            factoryBean.setExpectedType(DataSource.class);
            factoryBean.setProxyInterface(DataSource.class);
            factoryBean.setLookupOnStartup(true);
            factoryBean.afterPropertiesSet();

            log.info("Connected via JNDI: {}", jndiName);
            DataSource ds = (DataSource) factoryBean.getObject();
            if (ds == null)
                throw new IllegalArgumentException("DataSource from JNDI is null");

            return ds;
        } catch (Exception e) {
            log.warn("JNDI lookup failed ({}). Switching to Local JDBC Driver.", e.getMessage());

            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(dbProps.getDriverClassName());
            dataSource.setUrl(dbProps.getUrl());
            dataSource.setUsername(dbProps.getUsername());
            dataSource.setPassword(dbProps.getPassword());

            log.info("Connected via Local JDBC Driver: {}", dbProps.getUrl());
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

        props.put("hibernate.hbm2ddl.auto",
                dbProps.getHibernateHbm2ddlAuto() != null ? dbProps.getHibernateHbm2ddlAuto() : "validate");
        props.put("hibernate.show_sql",
                dbProps.getHibernateShowSql() != null ? dbProps.getHibernateShowSql() : "false");
        props.put("hibernate.format_sql",
                dbProps.getHibernateFormatSql() != null ? dbProps.getHibernateFormatSql() : "true");
        props.put("hibernate.generate_statistics",
                dbProps.getHibernateGenerateStatistics() != null ? dbProps.getHibernateGenerateStatistics() : "false");

        props.put("hibernate.jdbc.batch_size", String.valueOf(dbProps.getHibernateBatchSize()));
        props.put("hibernate.order_inserts",
                dbProps.getHibernateOrderInserts() != null ? dbProps.getHibernateOrderInserts() : "true");
        props.put("hibernate.order_updates",
                dbProps.getHibernateOrderUpdates() != null ? dbProps.getHibernateOrderUpdates() : "true");

        props.put("hibernate.connection.characterEncoding", "utf8");
        props.put("hibernate.connection.useUnicode", "true");
        props.put("hibernate.connection.charSet", "UTF-8");

        props.put("hibernate.jdbc.time_zone", "UTC");

        return props;
    }

    @Bean
    public PlatformTransactionManager transactionManager(@NonNull EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
