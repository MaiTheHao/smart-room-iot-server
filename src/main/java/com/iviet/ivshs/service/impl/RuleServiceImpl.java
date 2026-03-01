package com.iviet.ivshs.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.mapper.RuleMapper;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.schedule.rule.RuleJob;
import com.iviet.ivshs.schedule.rule.RuleProcessor;
import com.iviet.ivshs.service.DeviceControlStrategy;
import com.iviet.ivshs.service.RuleService;
import com.iviet.ivshs.util.QuartzUtil;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic="RuleService")
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

	private final RuleDao ruleDao;
	private final RuleMapper ruleMapper;
	private final RuleProcessor ruleProcessor;
	private final ObjectMapper objectMapper;
	private final QuartzUtil quartzUtil;

	// Control Strategies handle
	private final List<DeviceControlStrategy<?>> controlStrategies;
	private Map<DeviceCategory, DeviceControlStrategy<?>> controlStrategyMap;

	@PostConstruct
	public void handleDeviceControlStrategy() {
		controlStrategyMap = controlStrategies.stream().collect(Collectors.toMap(DeviceControlStrategy::getSupportedCategory, strategy -> strategy));
		log.info("Initialized Device Control Strategy Map with categories: {}", controlStrategyMap.keySet());
	}

	@Override
	@Transactional
	public void executeGlobalRuleScan() {
		List<Rule> activeRules = ruleDao.findAllActive();
		if (activeRules.isEmpty()) {
			log.debug("No active rules found during global scan.");
			return;
		}

		// Gom nhóm rule theo target (Category + DeviceId)
		Map<String, List<Rule>> rulesByTarget = activeRules.stream()
			.collect(Collectors.groupingBy(r -> r.getTargetDeviceCategory() + ":" + r.getTargetDeviceId()));

		rulesByTarget.forEach((targetKey, rules) -> {
			log.debug("Processing rules for target {}", targetKey);
			processTargetHighestPriorityRule(targetKey, rules);
		});
	}

	/**
	 * Xử lý kiểm tra Rule cho một target cụ thể.
	 * Chỉ lấy Rule có priority cao nhất. Nếu không thỏa mãn, bỏ qua các rule khác.
	 */
	private void processTargetHighestPriorityRule(String targetKey, List<Rule> rules) {
		Rule highestPriorityRule = findHighestPriorityRule(rules);

		if (highestPriorityRule == null) {
			return;
		}

		boolean isRuleSatisfied = ruleProcessor.matches(highestPriorityRule);

		if (isRuleSatisfied) {
			executeRuleAction(highestPriorityRule);
		} else {
			log.debug("Rule [{}] did not match for target [{}].", 
				highestPriorityRule.getName(), targetKey);
		}
	}

	/**
	 * Tìm Rule có priority cao nhất trong danh sách.
	 */
	private Rule findHighestPriorityRule(List<Rule> rules) {
		return rules.stream()
			.max(Comparator.comparing(Rule::getPriority))
			.orElse(null);
	}

	/**
	 * Đọc tham số và thực thi hành động điều khiển thiết bị.
	 */
	@SuppressWarnings("unchecked")
	private void executeRuleAction(Rule rule) {
		log.info("EXECUTING WINNER RULE: {} - Target: {}:{}", rule.getName(), rule.getTargetDeviceCategory(), rule.getTargetDeviceId());
		
		DeviceControlStrategy controlStrategy = controlStrategyMap.get(rule.getTargetDeviceCategory());
		if (controlStrategy == null) {
			log.error("No control strategy found for device category: {}. Cannot execute action for rule: {}", rule.getTargetDeviceCategory(), rule.getId());
			return;
		}

		try {
			var controlDtoClass = controlStrategy.getControlDtoClass();
			var controlRequestDto = objectMapper.treeToValue(rule.getActionParams(), controlDtoClass);
			var targetDeviceId = rule.getTargetDeviceId();
			controlStrategy.control(targetDeviceId, controlRequestDto);
			log.info("Successfully executed action for rule {} on device {}:{}", rule.getId(), rule.getTargetDeviceCategory(), targetDeviceId);
		} catch (JsonProcessingException e) {
			log.error("Invalid action params (JSON format error) for rule {}: {}", rule.getId(), e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error executing action for rule {}: {}", rule.getId(), e.getMessage(), e);
		}
	}

	@Override
	@Transactional
	public void reloadAllRules() {
		log.info("Request to reload all rules received. Triggering immediate rule scan.");
		try {
			org.quartz.JobKey jobKey = org.quartz.JobKey.jobKey(
				RuleJob.JOB_NAME, 
				RuleJob.JOB_GROUP
			);
			
			if (quartzUtil.checkExists(jobKey)) {
				quartzUtil.triggerJob(jobKey, null);
				log.info("Triggered immediate execution of RuleEngineJob: {}", jobKey);
			} else {
				log.warn("RuleEngineJob not found with key: {}", jobKey);
			}
		} catch (Exception e) {
			log.error("Failed to trigger rule reload: {}", e.getMessage(), e);
			throw new InternalServerErrorException("Failed to trigger rule reload", e);
		}
	}

	@Override
	@Transactional
	public RuleDto create(CreateRuleDto request) {
		if (ruleDao.existsByName(request.name())) {
			throw new BadRequestException("Rule name exists");
		}

		Rule rule = ruleMapper.fromCreateDto(request);
		Rule saved = ruleDao.save(rule);
		return ruleMapper.toDto(saved);
	}

	@Override
	@Transactional
	public RuleDto update(Long id, UpdateRuleDto request) {
		Rule existing = ruleDao.findById(id)
			.orElseThrow(() -> new NotFoundException("Rule not found"));

		// Update entity in-place using mapper
		ruleMapper.updateFromDto(request, existing);

		// JPA dirty checking will auto-persist changes
		// But we call update explicitly if needed by DAO pattern
		Rule result = ruleDao.update(existing);
		return ruleMapper.toDto(result);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		if (!ruleDao.existsById(id)) {
			throw new NotFoundException("Rule not found");
		}
		ruleDao.deleteById(id);
	}

	@Override
	public RuleDto getById(Long id) {
		Rule rule = ruleDao.findById(id)
			.orElseThrow(() -> new NotFoundException("Rule not found"));
		return ruleMapper.toDto(rule);
	}

	@Override
	public List<RuleDto> getAll() {
		List<Rule> rules = ruleDao.findAll();
		return ruleMapper.toDtoList(rules);
	}

	@Override
	public PaginatedResponse<RuleDto> getList(int page, int size) {
		List<Rule> rules = ruleDao.findAll(page, size);
		long total = ruleDao.count();
		return new PaginatedResponse<>(
			ruleMapper.toDtoList(rules),
			page, size, total
		);
	}

	@Override
	@Transactional
	public void toggleIsActive(Long id, boolean isActive) {
		Rule rule = ruleDao.findById(id).orElseThrow(() -> new NotFoundException("Rule not found"));
		rule.setIsActive(isActive);
		ruleDao.update(rule);
	}
}