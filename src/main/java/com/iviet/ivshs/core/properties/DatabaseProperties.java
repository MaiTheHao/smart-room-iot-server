package com.iviet.ivshs.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class DatabaseProperties {

  @Value("${app.jdbc.jndi-name:java:comp/env/jdbc/smartroom_db}")
  private String jndiName;

  @Value("${app.jdbc.driverClassName}")
  private String driverClassName;

  @Value("${app.jdbc.url}")
  private String url;

  @Value("${app.jdbc.username}")
  private String username;

  @Value("${app.jdbc.password}")
  private String password;

  @Value("${app.hibernate.hbm2ddl.auto:validate}")
  private String hibernateHbm2ddlAuto;

  @Value("${app.hibernate.show_sql:false}")
  private String hibernateShowSql;

  @Value("${app.hibernate.format_sql:true}")
  private String hibernateFormatSql;

  @Value("${app.hibernate.generate_statistics:false}")
  private String hibernateGenerateStatistics;

  @Value("${app.hibernate.jdbc.batch_size:50}")
  private int hibernateBatchSize;

  @Value("${app.hibernate.order_inserts:true}")
  private String hibernateOrderInserts;

  @Value("${app.hibernate.order_updates:true}")
  private String hibernateOrderUpdates;
}
