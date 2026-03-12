package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.RuleMapper;
import com.iviet.ivshs.schedule.rule.RuleJob;
import com.iviet.ivshs.schedule.rule.RuleProcessor;
import com.iviet.ivshs.service.RuleService;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;
import com.iviet.ivshs.util.QuartzUtil;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j(topic = "RuleService")
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

	private final RuleDao ruleDao;
	private final RuleMapper ruleMapper;
	private final RuleProcessor ruleProcessor;
	private final ObjectMapper objectMapper;
	private final QuartzUtil quartzUtil;
	private final Validator validator;
	private final List<DeviceControlServiceStrategy<?>> controlStrategies;

	private Map<DeviceCategory, DeviceControlServiceStrategy<?>> strategyMap;

	@PostConstruct
	private void initStrategyMap() {
			strategyMap = controlStrategies.stream()
							.collect(Collectors.toUnmodifiableMap(
											DeviceControlServiceStrategy::getSupportedCategory,
											Function.identity()
							));
			log.info("RuleEngine initialized with categories: {}", strategyMap.keySet());
	}

	@Override
	@Transactional(readOnly = true)
	public void executeGlobalRuleScan() {
			List<Rule> activeRules = ruleDao.findAllActive();
			if (activeRules.isEmpty()) return;

			activeRules.stream()
				.collect(Collectors
					.groupingBy(r -> r.getTargetDeviceCategory() + ":" + r.getTargetDeviceId(), Collectors.maxBy(Comparator.comparingInt(Rule::getPriority))))
					.forEach((target, optRule) -> optRule.ifPresent(this::processRule));
	}

	private void processRule(Rule rule) {
			if (ruleProcessor.matches(rule)) executeRuleAction(rule);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void executeRuleAction(Rule rule) {
			DeviceControlServiceStrategy strategy = strategyMap.get(rule.getTargetDeviceCategory());
			
			if (strategy == null) {
				log.error("Missing strategy for category: {}", rule.getTargetDeviceCategory());
				return;
			}

			try {
				Object controlDto = objectMapper.treeToValue(rule.getActionParams(), strategy.getControlDtoClass());
				strategy.control(rule.getTargetDeviceId(), controlDto);
				log.info("Executed rule [{}] for device [{}]", rule.getName(), rule.getTargetDeviceId());
			} catch (Exception e) {
				log.error("Failed to execute rule {}: {}", rule.getId(), e.getMessage());
			}
	}

	@Override
	@Transactional
	public RuleDto create(CreateRuleDto request) {
			if (ruleDao.existsByName(request.name())) {
				throw new BadRequestException("Rule name already exists");
			}
			validateActionParams(request.targetDeviceCategory(), request.actionParams());
			
			Rule saved = ruleDao.save(ruleMapper.fromCreateDto(request));
			return ruleMapper.toDto(saved);
	}

	@Override
	@Transactional
	public RuleDto update(Long id, UpdateRuleDto request) {
		Rule existing = ruleDao.findById(id).orElseThrow(() -> new NotFoundException("Rule not found"));

		DeviceCategory category = Objects.requireNonNullElse(request.targetDeviceCategory(), existing.getTargetDeviceCategory());
		JsonNode params = Objects.requireNonNullElse(request.actionParams(), existing.getActionParams());

		if (request.targetDeviceCategory() != null && request.actionParams() == null) {
			throw new BadRequestException("New category requires specific action parameters");
		}

		validateActionParams(category, params);
		ruleMapper.updateFromDto(request, existing);
		
		return ruleMapper.toDto(ruleDao.update(existing));
	}

	@Override
	@Transactional
	public void toggleIsActive(Long id, boolean isActive) {
		Rule rule = ruleDao.findById(id).orElseThrow(() -> new NotFoundException("Rule not found"));
		rule.setIsActive(isActive);
		ruleDao.update(rule);
	}

	private void validateActionParams(DeviceCategory category, JsonNode params) {
		DeviceControlServiceStrategy<?> strategy = Optional.ofNullable(strategyMap.get(category))
						.orElseThrow(() -> new BadRequestException("Unsupported category: " + category));

		try {
			Object dto = objectMapper.treeToValue(params, strategy.getControlDtoClass());
			var violations = validator.validate(dto);
			if (!violations.isEmpty()) {
				String errorMsg = violations.stream()
					.map(v -> v.getPropertyPath() + " " + v.getMessage())
					.collect(Collectors.joining(", "));
				throw new BadRequestException(errorMsg);
			}
		} catch (JsonProcessingException e) {
			throw new BadRequestException("Invalid JSON format for action parameters");
		}
	}

	@Override
	public void reloadAllRules() {
		JobKey jobKey = JobKey.jobKey(RuleJob.JOB_NAME, RuleJob.JOB_GROUP);
		try {
			if (quartzUtil.checkExists(jobKey)) {
				quartzUtil.triggerJob(jobKey, null);
			}
		} catch (Exception e) {
			throw new InternalServerErrorException("Quartz trigger failed", e);
		}
	}

	@Override public List<RuleDto> getAll() { return ruleMapper.toDtoList(ruleDao.findAll()); }
	@Override public RuleDto getById(Long id) { return ruleDao.findById(id).map(ruleMapper::toDto).orElseThrow(() -> new NotFoundException("Rule not found")); }
	@Override @Transactional public void delete(Long id) { if (!ruleDao.existsById(id)) throw new NotFoundException("Rule not found"); ruleDao.deleteById(id); }
	@Override public PaginatedResponse<RuleDto> getList(int page, int size) { 
			return new PaginatedResponse<>(ruleMapper.toDtoList(ruleDao.findAll(page, size)), page, size, ruleDao.count()); 
	}
}