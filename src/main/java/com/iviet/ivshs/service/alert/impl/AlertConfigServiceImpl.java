package com.iviet.ivshs.service.alert.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.dto.alert.CreateAlertConfigDto;
import com.iviet.ivshs.dto.alert.UpdateAlertConfigDto;
import com.iviet.ivshs.dto.alert.AlertConfigDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.SysGroup;
import com.iviet.ivshs.service.alert.AlertConfigService;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.exception.NotFoundException;
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

    @Override
    public List<AlertConfigDto> getConfigsBySource(AlertNamespace namespace, String sourceId) {
        return alertConfigDao.findAllByNamespaceAndSourceId(namespace, sourceId).stream().map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<AlertConfigDto> getAllConfigs(AlertNamespace namespace, int page, int size) {
        List<AlertConfig> configs = alertConfigDao.findAll(namespace, page, size);
        long total = alertConfigDao.countAll(namespace);
        List<AlertConfigDto> dtos = configs.stream().map(this::toResponseDto).toList();
        return new PaginatedResponse<>(dtos, page, size, total);
    }

    @Override
    public AlertConfigDto getConfigById(Long id) {
        AlertConfig config = alertConfigDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + id));
        return toResponseDto(config);
    }

    @Override
    @Transactional
    public AlertConfigDto createConfig(CreateAlertConfigDto dto) {
        AlertConfig config = dto.toEntity(objectMapper);
        alertConfigDao.save(config);

        saveGroupAssociations(config, dto.recipientGroupCodes());

        log.info("[AlertConfig] Created config id={} namespace={} source={}", config.getId(), dto.namespace(),
                dto.sourceId());
        return toResponseDto(config);
    }

    @Override
    @Transactional
    public AlertConfigDto updateConfig(Long id, UpdateAlertConfigDto dto) {
        AlertConfig config = alertConfigDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + id));

        dto.updateEntity(config, objectMapper);
        alertConfigDao.update(config);

        // Xóa và tạo lại group associations
        alertConfigDao.deleteGroupAssociations(id);
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
            alertConfigDao.saveGroupAssociation(config, group);
        }
    }

    private AlertConfigDto toResponseDto(AlertConfig config) {
        List<String> groupCodes = alertConfigDao.findGroupCodesByConfigId(config.getId());

        List<String> channels = new ArrayList<>();
        if (config.getChannels() != null && config.getChannels().isArray()) {
            config.getChannels().forEach(node -> channels.add(node.asText()));
        }

        return AlertConfigDto.from(config, groupCodes, channels);
    }
}
