package com.iviet.ivshs.config;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.iviet.ivshs.component.AutowiringSpringBeanJobFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class QuartzConfig {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private Environment env;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Bean(name = "quartzTaskExecutor")
	public Executor quartzTaskExecutor() {
		log.info("Initializing Quartz TaskExecutor with Virtual Threads (JDK 21+)");
		return Executors.newVirtualThreadPerTaskExecutor();
	}

	@Bean
	public AutowiringSpringBeanJobFactory springBeanJobFactory() {
		AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		return jobFactory;
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() {
		log.info("Configuring Quartz SchedulerFactoryBean");
		SchedulerFactoryBean factory = new SchedulerFactoryBean();

		factory.setDataSource(dataSource);
		factory.setTransactionManager(transactionManager);
		factory.setTaskExecutor(quartzTaskExecutor());
		factory.setJobFactory(springBeanJobFactory());
		
		// Luôn ghi đè job nếu đã tồn tại trong DB khi khởi động
		factory.setOverwriteExistingJobs(true);
		factory.setAutoStartup(true);
		factory.setWaitForJobsToCompleteOnShutdown(true);

		factory.setQuartzProperties(createQuartzProperties());

		return factory;
	}

	private Properties createQuartzProperties() {
		Properties props = new Properties();

		props.put("org.quartz.jobStore.dataSource", "ds"); 
		props.put("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
		
		props.put("org.quartz.jobStore.isClustered", "false"); 
		
		props.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
		props.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
		
		props.put("org.quartz.scheduler.instanceName", "IVSHS_Quartz_Scheduler");
		props.put("org.quartz.scheduler.instanceId", "AUTO");
		props.put("org.quartz.jobStore.misfireThreshold", "60000");

		return props;
	}
}