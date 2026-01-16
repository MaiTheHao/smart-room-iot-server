package com.iviet.ivshs.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import com.iviet.ivshs.service.AutomationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Startup")
@Component
@Order(20)
@RequiredArgsConstructor
public class QuartzInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private final AutomationService automationService;
	private boolean isInitialized = false;

	@Override
	public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
		if (isInitialized) {
			return;
		}

		try {
			log.info("Module       : [Quartz Automation] -> [RUNNING]");
			
			long startTime = System.currentTimeMillis();
			automationService.reloadAllAutomations();
			long duration = System.currentTimeMillis() - startTime;

			log.info("Module       : [Quartz Automation] -> [OK]");
			log.info("  - Duration   : {} ms", duration);

			isInitialized = true;

		} catch (Exception e) {
			log.error("Module       : [Quartz Automation] -> [FAILED]");
			log.error("  - Reason     : {}", e.getMessage());
			log.error("Stack trace:", e);
		}
	}
}
