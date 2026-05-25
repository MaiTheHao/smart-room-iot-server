package com.iviet.ivshs.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.RuleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(21)
@RequiredArgsConstructor
public class RuleInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final RuleService ruleService;
    private boolean isInitialized = false;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (isInitialized) {
            return;
        }

        try {
            log.info("Starting Rule Engine initialization");
            
            long startTime = System.currentTimeMillis();
            ruleService.reloadAllRules();
            long duration = System.currentTimeMillis() - startTime;

            log.info("Rule Engine initialized successfully: duration={}ms", duration);

            isInitialized = true;

        } catch (Exception e) {
            log.error("Rule Engine initialization failed", e);
            log.warn("Server proceeding without Rule Engine. Please check database/Quartz scheduler status.");
        }
    }
}
