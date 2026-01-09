package com.iviet.ivshs.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Startup")
@Component
@Order(1)
public class ApplicationStartupLogger implements ApplicationListener<ContextRefreshedEvent> {

    private boolean isLogged = false;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (isLogged) {
            return;
        }

        String[] activeProfiles = event.getApplicationContext().getEnvironment().getActiveProfiles();
        String profiles = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "default";
        int beanCount = event.getApplicationContext().getBeanDefinitionCount();

        log.info("============================================================");
        log.info("SYSTEM STARTUP :: SMART ROOM IOT SYSTEM");
        log.info("------------------------------------------------------------");
        log.info("Context      : {}", event.getApplicationContext().getDisplayName());
        log.info("Profiles     : [{}]", profiles);
        log.info("Components   : {} beans loaded", beanCount);
        log.info("Status       : INITIALIZING...");
        log.info("------------------------------------------------------------");

        isLogged = true;
    }
}
