package com.iviet.ivshs.core.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(999)
public class ContextRefreshedLogger implements ApplicationListener<ContextRefreshedEvent> {

    private boolean isLogged = false;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (isLogged) {
            return;
        }

        String port = event.getApplicationContext().getEnvironment().getProperty("server.port", "8080");
        String timestamp = Instant.now()
                .atZone(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"));

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
