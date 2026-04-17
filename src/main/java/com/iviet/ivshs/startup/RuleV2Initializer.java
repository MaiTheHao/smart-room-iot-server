package com.iviet.ivshs.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.RuleV2Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "INIT-RULE-V2")
@Component
@Order(21)
@RequiredArgsConstructor
public class RuleV2Initializer implements ApplicationListener<ContextRefreshedEvent> {

    private final RuleV2Service ruleV2Service;
    private boolean isInitialized = false;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (isInitialized) {
            return;
        }

        try {
            log.info("Module       : [Quartz RuleV2] -> [RUNNING]");
            
            long startTime = System.currentTimeMillis();
            ruleV2Service.reloadAllRules();
            long duration = System.currentTimeMillis() - startTime;

            log.info("Module       : [Quartz RuleV2] -> [OK]");
            log.info("  - Duration   : {} ms", duration);

            isInitialized = true;

        } catch (Exception e) {
            log.error("Module       : [Quartz RuleV2] -> [FAILED]");
            log.error("  - Reason     : {}", e.getMessage());
            log.error("------------------------------------------------------------");
            log.error("Stack trace:", e);
        }
    }
}
