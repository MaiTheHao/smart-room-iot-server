package com.iviet.ivshs.startup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.ClientFunctionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Startup")
@Component
@Order(10)
public class PermissionCacheInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ClientFunctionService sysClientFunctionCacheService;

	@Autowired
	private Environment env;

    private boolean isInitialized = false;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (isInitialized) {
            return;
        }

		boolean enabled = env.getProperty("startup.permissionCache.enabled", Boolean.class, false);
		if (!enabled) {
			log.info("Module       : [Permission Cache] -> [SKIP]");
			return;
		}

        try {
            log.info("Module       : [Permission Cache] -> [RUNNING]");
            
            long startTime = System.currentTimeMillis();
            int totalEntries = sysClientFunctionCacheService.rebuildAllCache();
            long duration = System.currentTimeMillis() - startTime;

            log.info("Module       : [Permission Cache] -> [OK]");
            log.info("  - Entries    : {} items", totalEntries);
            log.info("  - Duration   : {} ms", duration);

            isInitialized = true;

        } catch (Exception e) {
            log.error("Module       : [Permission Cache] -> [FAILED]");
            log.error("  - Reason     : {}", e.getMessage());
            log.error("------------------------------------------------------------");
            log.error("Stack trace:", e);
            log.warn("WARNING: Server proceeding without permission cache");
            log.warn("ACTION: Rebuild manually via API or scheduled task");
        }
    }
}
