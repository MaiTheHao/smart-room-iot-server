package com.iviet.ivshs.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import com.iviet.ivshs.service.AutomationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class AutomationInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private final AutomationService automationService;
	private boolean isInitialized = false;

	@Override
	public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
		if (isInitialized) {
			return;
		}

		try {
			log.info("Starting Automation Engine initialization");
			
			long startTime = System.currentTimeMillis();
			automationService.reloadAllAutomations();
			long duration = System.currentTimeMillis() - startTime;

			log.info("Automation Engine initialized successfully: duration={}ms", duration);

			isInitialized = true;

		} catch (Exception e) {
			log.error("Automation Engine initialization failed", e);
			log.warn("Server proceeding without Automation Engine. Please check database/Quartz scheduler status.");
		}
	}
}
