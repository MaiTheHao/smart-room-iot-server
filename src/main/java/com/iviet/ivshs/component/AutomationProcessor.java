package com.iviet.ivshs.component;

import java.util.Comparator;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.entities.Automation;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.enumeration.JobActionType;
import com.iviet.ivshs.service.LightService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutomationProcessor {

	private final LightService lightService;

	@Transactional
	public void process(Automation automation) {
		log.info("Processing automation: {} [ID: {}]", automation.getName(), automation.getId());

		automation.getActions().stream()
				.sorted(Comparator.comparingInt(AutomationAction::getExecutionOrder))
				.forEach(action -> {
					try {
						routeAction(action);
					} catch (Exception e) {
						log.error("Action failed [ID: {}]: {}", action.getId(), e.getMessage(), e);
					}
				});

		log.info("Completed automation: {}", automation.getName());
	}

	private void routeAction(AutomationAction action) {
		switch (action.getTargetType()) {
			case LIGHT -> processLight(action);
			default -> log.warn("Unknown target type: {}", action.getTargetType());
		}
	}

	private void processLight(AutomationAction action) {
		log.info("Processing LIGHT action: ID={}, Action={}", action.getTargetId(), action.getActionType());

		boolean newState = (action.getActionType() == JobActionType.ON);
		try {
			lightService.handleStateControl(action.getTargetId(), newState);
			log.info("Light {} -> {}", action.getTargetId(), newState ? "ON" : "OFF");
		} catch (Exception e) {
			log.error("Failed to process light action [ID: {}]: {}", action.getTargetId(), e.getMessage(), e);
			throw e;
		}
	}
}
