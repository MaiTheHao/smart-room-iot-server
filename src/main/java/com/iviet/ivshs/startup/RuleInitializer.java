package com.iviet.ivshs.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.RuleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "INIT-RULE")
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
            log.info("Module       : [Quartz Rule] -> [RUNNING]");
            
            long startTime = System.currentTimeMillis();
            ruleService.reloadAllRules();
            long duration = System.currentTimeMillis() - startTime;

            log.info("Module       : [Quartz Rule] -> [OK]");
            log.info("  - Duration   : {} ms", duration);

            isInitialized = true;

        } catch (Exception e) {
            log.error("Module       : [Quartz Rule] -> [FAILED]");
            log.error("  - Reason     : {}", e.getMessage());
            log.error("------------------------------------------------------------");
            log.error("Stack trace:", e);
            log.warn("WARNING      : Server proceeding without rule engine");
            log.warn("ACTION       : Check logs and restart server if needed");
        }
    }
}
