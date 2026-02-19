package com.iviet.ivshs.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.schedule.rule.RuleJob;
import com.iviet.ivshs.schedule.rule.RuleProcessor;
import com.iviet.ivshs.service.RuleService;
import com.iviet.ivshs.util.QuartzUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

	private final RuleDao ruleDao;
	private final RuleProcessor ruleProcessor;
	private final ObjectMapper objectMapper;
	private final QuartzUtil quartzUtil;

	@Override
	@Transactional
	public void executeGlobalRuleScan() {
		List<Rule> activeRules = ruleDao.findAllActive();
		if (activeRules.isEmpty()) return;

		Map<String, List<Rule>> rulesByTarget = activeRules.stream()
			.collect(Collectors.groupingBy(r -> r.getTargetDeviceCategory() + ":" + r.getTargetDeviceId()));

		rulesByTarget.forEach((targetKey, rules) -> {
			evaluateTargetRules(targetKey, rules);
		});
	}

	private void evaluateTargetRules(String targetKey, List<Rule> rules) {
		List<Rule> satisfiedRules = rules.stream()
			.filter(ruleProcessor::matches)
			.collect(Collectors.toList());

		if (satisfiedRules.isEmpty()) {
			return;
		}

		Rule winner = satisfiedRules.stream()
			.sorted(Comparator.comparing(Rule::getPriority).reversed())
			.findFirst()
			.orElse(null);

		if (winner != null) {
			log.info("EXECUTING WINNER RULE: {} - Target: {}:{}", winner.getName(), winner.getTargetDeviceCategory(), winner.getTargetDeviceId());
			try {
				JsonNode params = objectMapper.readTree(winner.getActionParams());
				log.info("Action Params: {}", params.toString());
				// TODO: Hoàn thiện chổ này, môt method để tiến hành điều khiển các thiết bị
			} catch (JsonProcessingException e) {
				log.error("Invalid action params for rule {}: {}", winner.getId(), e.getMessage());
			}
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

		Rule rule = request.toEntity();
		Rule saved = ruleDao.save(rule);
		return RuleDto.from(saved);
	}

	@Override
	@Transactional
	public RuleDto update(Long id, UpdateRuleDto request) {
		Rule existing = ruleDao.findById(id)
			.orElseThrow(() -> new NotFoundException("Rule not found"));

		Rule updated = request.toEntity(id);
		existing.setId(updated.getId());
		existing.setName(updated.getName());
		existing.setPriority(updated.getPriority());
		existing.setTargetDeviceId(updated.getTargetDeviceId());
		existing.setTargetDeviceCategory(updated.getTargetDeviceCategory());
		existing.setActionParams(updated.getActionParams());
		
		if (request.isActive() != null) {
			existing.setIsActive(request.isActive());
		}

		existing.getConditions().clear();
		existing.getConditions().addAll(updated.getConditions());

		Rule result = ruleDao.update(existing);
		return RuleDto.from(result);
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
		return RuleDto.from(rule);
	}

	@Override
	public List<RuleDto> getAll() {
		List<Rule> rules = ruleDao.findAll(0, 100);
		return RuleDto.fromEntities(rules);
	}

	@Override
	@Transactional
	public void toggleIsActive(Long id, boolean isActive) {
		Rule rule = ruleDao.findById(id)
			.orElseThrow(() -> new NotFoundException("Rule not found"));
		rule.setIsActive(isActive);
		ruleDao.update(rule);
	}
}
