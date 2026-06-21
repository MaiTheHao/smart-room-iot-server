package com.iviet.ivshs.service.alert.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.dto.alert.AlertConfigDto;
import com.iviet.ivshs.dto.alert.AlertConfigResponseDto;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.AlertConfigGroup;
import com.iviet.ivshs.entities.SysGroup;
import com.iviet.ivshs.service.alert.AlertConfigService;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertConfigServiceImpl implements AlertConfigService {

    private final AlertConfigDao alertConfigDao;
    private final SysGroupDao sysGroupDao;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AlertConfigResponseDto> getConfigsBySource(AlertNamespace namespace, String sourceId) {
        return alertConfigDao.findAllByNamespaceAndSourceId(namespace, sourceId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public AlertConfigResponseDto getConfigById(Long id) {
        AlertConfig config = alertConfigDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + id));
        return toResponseDto(config);
    }

    @Override
    @Transactional
    public AlertConfigResponseDto createConfig(AlertConfigDto dto) {
        AlertConfig config = AlertConfig.builder()
                .namespace(dto.namespace())
                .alertCode(dto.alertCode())
                .sourceId(dto.sourceId())
                .alertName(dto.alertName())
                .severity(dto.severity())
                .channels(objectMapper.valueToTree(dto.channels()))
                .messageTemplate(dto.messageTemplate())
                .cooldownMinutes(dto.cooldownMinutes())
                .autoResolve(dto.autoResolve())
                .build();
        alertConfigDao.save(config);

        // Lưu group associations
        saveGroupAssociations(config, dto.recipientGroupCodes());

        log.info("[AlertConfig] Created config id={} namespace={} source={}", config.getId(), dto.namespace(), dto.sourceId());
        return toResponseDto(config);
    }

    @Override
    @Transactional
    public AlertConfigResponseDto updateConfig(Long id, AlertConfigDto dto) {
        AlertConfig config = alertConfigDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + id));

        config.setAlertName(dto.alertName());
        config.setSeverity(dto.severity());
        config.setChannels(objectMapper.valueToTree(dto.channels()));
        config.setMessageTemplate(dto.messageTemplate());
        config.setCooldownMinutes(dto.cooldownMinutes());
        config.setAutoResolve(dto.autoResolve());
        alertConfigDao.update(config);

        // Xóa và tạo lại group associations
        entityManager.createQuery("DELETE FROM AlertConfigGroup acg WHERE acg.alertConfig.id = :configId")
                .setParameter("configId", id)
                .executeUpdate();
        saveGroupAssociations(config, dto.recipientGroupCodes());

        return toResponseDto(config);
    }

    @Override
    @Transactional
    public void deleteConfig(Long id) {
        AlertConfig config = alertConfigDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + id));
        alertConfigDao.delete(config);
        log.info("[AlertConfig] Deleted config id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllBySource(AlertNamespace namespace, String sourceId) {
        int deleted = alertConfigDao.deleteAllByNamespaceAndSourceId(namespace, sourceId);
        log.info("[AlertConfig] Deleted {} configs for namespace={} sourceId={}", deleted, namespace, sourceId);
    }

    // ===== Private helpers =====

    private void saveGroupAssociations(AlertConfig config, List<String> groupCodes) {
        if (groupCodes == null || groupCodes.isEmpty()) return;
        for (String code : groupCodes) {
            SysGroup group = sysGroupDao.findEntityByCode(code)
                    .orElseThrow(() -> new NotFoundException("Group not found: " + code));
            AlertConfigGroup acg = AlertConfigGroup.builder()
                    .alertConfig(config)
                    .group(group)
                    .build();
            entityManager.persist(acg);
        }
    }

    private AlertConfigResponseDto toResponseDto(AlertConfig config) {
        List<String> groupCodes = entityManager.createQuery(
                "SELECT acg.group.groupCode FROM AlertConfigGroup acg WHERE acg.alertConfig.id = :id", String.class)
                .setParameter("id", config.getId())
                .getResultList();

        List<String> channels = new ArrayList<>();
        if (config.getChannels() != null && config.getChannels().isArray()) {
            config.getChannels().forEach(node -> channels.add(node.asText()));
        }

        return AlertConfigResponseDto.from(config, groupCodes, channels);
    }
}
