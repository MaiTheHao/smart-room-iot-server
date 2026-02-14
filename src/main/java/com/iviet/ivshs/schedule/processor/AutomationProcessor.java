package com.iviet.ivshs.schedule.processor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.schedule.handler.AutomationActionHandler;
import com.iviet.ivshs.entities.Automation;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.enumeration.JobTargetType;
import com.iviet.ivshs.exception.domain.BaseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "AUTOMATION-PROCESSOR")
@Component
public class AutomationProcessor {

    private final Map<JobTargetType, AutomationActionHandler> actionHandlerMap;

    public AutomationProcessor(List<AutomationActionHandler> actionHandlers) {
        this.actionHandlerMap = actionHandlers.stream()
                .collect(Collectors.toMap(AutomationActionHandler::getTargetType, Function.identity()));
        
        log.info("Processor initialized with {} handlers", actionHandlerMap.size());
    }

    public void process(Automation automation) {
        if (automation.getActions() == null || automation.getActions().isEmpty()) {
            return;
        }

        log.info("Executing: {} [ID: {}]", automation.getName(), automation.getId());
        long start = System.currentTimeMillis();

        automation.getActions().stream()
                .sorted(Comparator.comparingInt(AutomationAction::getExecutionOrder))
                .forEach(this::dispatchAction);

        log.info("Completed: {} in {}ms", automation.getName(), System.currentTimeMillis() - start);
    }

    private void dispatchAction(AutomationAction action) {
        JobTargetType type = action.getTargetType();
        AutomationActionHandler handler = actionHandlerMap.get(type);

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
