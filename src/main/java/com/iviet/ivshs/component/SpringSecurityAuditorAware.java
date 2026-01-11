package com.iviet.ivshs.component;

import com.iviet.ivshs.util.RequestContextUtil;
import com.iviet.ivshs.util.SecurityContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j(topic = "Auditing")
@Component("auditorProvider")
public class SpringSecurityAuditorAware implements AuditorAware<String> {
	private static final String SYSTEM_AUDITOR = "SYSTEM";
	private static final String ANONYMOUS_AUDITOR = "ANONYMOUS";

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        if (RequestContextUtil.isInternalCall()) {
            log.debug("Internal system call detected, setting auditor to SYSTEM");
            return Optional.of(SYSTEM_AUDITOR);
        }

        try {
            String username = SecurityContextUtil.getCurrentUsername();
            log.debug("Authenticated user detected, setting auditor to {}", username);
            return Optional.ofNullable(username);
        } catch (Exception e) {
            log.debug("No authenticated user found, setting auditor to ANONYMOUS");
            return Optional.of(ANONYMOUS_AUDITOR);
        }
    }
}
