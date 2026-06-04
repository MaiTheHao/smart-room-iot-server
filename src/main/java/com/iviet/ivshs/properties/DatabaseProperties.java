package com.iviet.ivshs.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class DatabaseProperties {
    
    @Value("${jdbc.jndi-name:java:comp/env/jdbc/smartroom_db}")
    private String jndiName;
    
    @Value("${jdbc.driverClassName}")
    private String driverClassName;
    
    @Value("${jdbc.url}")
    private String url;
    
    @Value("${jdbc.username}")
    private String username;
    
    @Value("${jdbc.password}")
    private String password;
    
    @Value("${hibernate.hbm2ddl.auto:validate}")
    private String hibernateHbm2ddlAuto;
    
    @Value("${hibernate.show_sql:false}")
    private String hibernateShowSql;
    
    @Value("${hibernate.format_sql:true}")
    private String hibernateFormatSql;
    
    @Value("${hibernate.generate_statistics:false}")
    private String hibernateGenerateStatistics;
    
    @Value("${hibernate.jdbc.batch_size:50}")
    private int hibernateBatchSize;
    
    @Value("${hibernate.order_inserts:true}")
    private String hibernateOrderInserts;
    
    @Value("${hibernate.order_updates:true}")
    private String hibernateOrderUpdates;
}

