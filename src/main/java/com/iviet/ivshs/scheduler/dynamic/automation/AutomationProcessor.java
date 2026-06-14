package com.iviet.ivshs.scheduler.dynamic.automation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.entities.Automation;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.scheduler.dynamic.automation.strategy.AutomationActionStrategy;
import com.iviet.ivshs.shared.enumeration.JobTargetType;
import com.iviet.ivshs.shared.exception.BaseException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.scheduler.dynamic.base.SchedulableJobProcessor;
import com.iviet.ivshs.scheduler.dynamic.base.JobProcessorType;
import com.iviet.ivshs.dao.AutomationDao;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AutomationProcessor implements SchedulableJobProcessor {

    private final Map<JobTargetType, AutomationActionStrategy> actionHandlerMap;
    private final AutomationDao automationDao;

    public AutomationProcessor(List<AutomationActionStrategy> actionHandlers, AutomationDao automationDao) {
        this.actionHandlerMap = actionHandlers.stream().collect(Collectors.toMap(AutomationActionStrategy::getTargetType, Function.identity()));
        this.automationDao = automationDao;

        log.info("Processor initialized with {} handlers", actionHandlerMap.size());
    }

    @Override
    public JobProcessorType getProcessorType() {
        return JobProcessorType.AUTOMATION;
    }

    @Override
    @Transactional
    public void processJob(Long id) {
        Automation automation = automationDao.findByIdWithActions(id)
            .orElseThrow(() -> new NotFoundException("Automation not found: " + id));
        if (Boolean.FALSE.equals(automation.getIsActive())) return;
        this.process(automation);
    }

    public void process(Automation automation) {
        if (automation.getActions() == null || automation.getActions().isEmpty()) {
            return;
        }

        log.info("Executing: {} [ID: {}]", automation.getName(), automation.getId());
        long start = System.currentTimeMillis();

        automation.getActions().stream().sorted(Comparator.comparingInt(AutomationAction::getExecutionOrder)).forEach(this::dispatchAction);

        log.info("Completed: {} in {}ms", automation.getName(), System.currentTimeMillis() - start);
    }

    private void dispatchAction(AutomationAction action) {
        JobTargetType type = action.getTargetType();
        AutomationActionStrategy handler = actionHandlerMap.get(type);

        if (handler == null) {
            log.warn("Skip: No handler for type {}", type);
            return;
        }

        try {
            handler.handle(action);
        } catch (Exception e) {
            String errorMsg = (e instanceof BaseException) ? e.getMessage() : "Unexpected error occurred";
            log.warn("Failed [ID: {}, Type: {}]: {}", action.getId(), type, errorMsg);
        }
    }
}
