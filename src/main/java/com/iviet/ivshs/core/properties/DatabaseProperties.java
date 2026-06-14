package com.iviet.ivshs.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class DatabaseProperties {

  @Value("${app.jdbc.jndi-name}")
  private String jndiName;

  @Value("${app.jdbc.driverClassName}")
  private String driverClassName;

  @Value("${app.jdbc.url}")
  private String url;

  @Value("${app.jdbc.username}")
  private String username;

  @Value("${app.jdbc.password}")
  private String password;

  @Value("${app.hibernate.hbm2ddl.auto}")
  private String hibernateHbm2ddlAuto;

  @Value("${app.hibernate.show_sql}")
  private String hibernateShowSql;

  @Value("${app.hibernate.format_sql}")
  private String hibernateFormatSql;

  @Value("${app.hibernate.generate_statistics}")
  private String hibernateGenerateStatistics;

  @Value("${app.hibernate.jdbc.batch_size}")
  private int hibernateBatchSize;

  @Value("${app.hibernate.order_inserts}")
  private String hibernateOrderInserts;

  @Value("${app.hibernate.order_updates}")
  private String hibernateOrderUpdates;
}
