package com.iviet.ivshs.startup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.ClientFunctionCacheServiceV1;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "StartupLogger")
@Component
@Order(10)
public class PermissionCacheInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ClientFunctionCacheServiceV1 sysClientFunctionCacheService;

	@Autowired
	private Environment env;

    private boolean isInitialized = false;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (isInitialized) {
            log.debug("Permission cache already initialized. Skipping...");
            return;
        }

		if (!env.getProperty("startup.permissionCache.enabled", Boolean.class, false)) {
			log.info("Permission cache initialization is disabled via configuration. Skipping...");
			return;
		}

        try {
            log.info("⚡ [Permission Cache] Rebuild sequence initiated...");
            
            long startTime = System.currentTimeMillis();

            int totalEntries = sysClientFunctionCacheService.rebuildAllCache();

            long duration = System.currentTimeMillis() - startTime;

            log.info("✅ [Permission Cache] Rebuild successful");
            log.info("   ├─ 📊 Status   : COMPLETED");
            log.info("   ├─ 📥 Entries  : {} items", totalEntries);
            log.info("   └─ ⏱️  Duration : {} ms", duration);

            isInitialized = true;

        } catch (Exception e) {
            log.error("❌ [Permission Cache] Critical failure");
            log.error("   ├─ ⚠️  Error    : {}", e.getMessage());
            log.error("   └─ 🛠️  Action   : Manual rebuild required");
            log.error("────────────────────────────────────────────────────────────");
            log.error("Stack trace:", e);
            log.warn("⚠️  Server proceeding without permission cache!");
            log.warn("💡 Tip: Rebuild manually via API or scheduled task.");
        }
    }
}
