package com.iviet.ivshs.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Startup")
@Component
@Order(999)
public class ApplicationStartupCompleteLogger implements ApplicationListener<ContextRefreshedEvent> {

    private boolean isLogged = false;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (isLogged) {
            return;
        }

        String port = event.getApplicationContext().getEnvironment().getProperty("server.port", "8080");
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("------------------------------------------------------------");
        log.info("SERVER READY");
        log.info("------------------------------------------------------------");
        log.info("Access URL   : http://localhost:{}", port);
        log.info("Timestamp    : {}", timestamp);
        log.info("Status       : ALL SYSTEMS OPERATIONAL");
        log.info("============================================================");

        isLogged = true;
    }
}
