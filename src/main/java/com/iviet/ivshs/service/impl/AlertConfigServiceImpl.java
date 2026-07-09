package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dto.AlertConfigDto;
import com.iviet.ivshs.dto.AlertConfigFilterDto;
import com.iviet.ivshs.dto.CreateAlertConfigDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAlertConfigDto;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.AlertConfigGroup;
import com.iviet.ivshs.entities.SysGroup;
import com.iviet.ivshs.service.AlertConfigService;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertConfigServiceImpl implements AlertConfigService {

    private final AlertConfigDao alertConfigDao;
    private final SysGroupDao sysGroupDao;
    private final RuleDao ruleDao;
    private final ClientDao clientDao;
    private final ObjectMapper objectMapper;

    @Override
    public List<AlertConfigDto> getConfigsBySource(AlertNamespace namespace, String sourceId) {
        List<AlertConfig> configs = alertConfigDao.findAllByNamespaceAndSourceId(namespace, sourceId);
        List<Long> configIds = configs.stream().map(AlertConfig::getId).toList();
        List<AlertConfigGroup> groups = alertConfigDao.findAssociationsByConfigIds(configIds);
        return AlertConfigDto.toDtos(configs, groups);
    }

    @Override
    public PaginatedResponse<AlertConfigDto> getAllConfigs(AlertNamespace namespace, int page, int size) {
        List<AlertConfig> configs = alertConfigDao.findAll(namespace, page, size);
        long total = alertConfigDao.countAll(namespace);
        List<Long> configIds = configs.stream().map(AlertConfig::getId).toList();
        List<AlertConfigGroup> groups = alertConfigDao.findAssociationsByConfigIds(configIds);
        List<AlertConfigDto> dtos = AlertConfigDto.toDtos(configs, groups);
        return new PaginatedResponse<>(dtos, page, size, total);
    }

    @Override
    public PaginatedResponse<AlertConfigDto> getAllConfigs(AlertConfigFilterDto filter) {
        List<AlertConfig> configs = alertConfigDao.findAllByFilter(filter);
        long total = alertConfigDao.countAllByFilter(filter);
        List<Long> configIds = configs.stream().map(AlertConfig::getId).toList();
        List<AlertConfigGroup> groups = alertConfigDao.findAssociationsByConfigIds(configIds);
        List<AlertConfigDto> dtos = AlertConfigDto.toDtos(configs, groups);
        return new PaginatedResponse<>(dtos, filter.page(), filter.size(), total);
    }

    @Override
    public long countConfigs(AlertConfigFilterDto filter) {
        return alertConfigDao.countAllByFilter(filter);
    }

    @Override
    public AlertConfigDto getConfigById(Long id) {
        AlertConfig config = alertConfigDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + id));
        List<AlertConfigGroup> groups = alertConfigDao.findAssociationsByConfigIds(List.of(id));
        return AlertConfigDto.toDto(config, groups);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlertConfigDto createConfig(CreateAlertConfigDto dto) {
        // Validate recipient groups exist
        validateGroupCodes(dto.recipientGroupCodes());

        // Validate sourceId existence depending on namespace
        validateSourceId(dto.namespace(), dto.sourceId());

        // Check for duplicate alertCode under the same namespace and sourceId
        if (alertConfigDao.findByPolymorphicKey(dto.namespace(), dto.alertCode(), dto.sourceId()).isPresent()) {
            throw new BadRequestException("Alert configuration with alertCode '" + dto.alertCode()
                    + "' already exists for this namespace and source.");
        }

        AlertConfig config = dto.toEntity(objectMapper);
        alertConfigDao.save(config);

        saveGroupAssociations(config, dto.recipientGroupCodes());

        log.info("[AlertConfig] Created config id={} namespace={} source={}", config.getId(), dto.namespace(),
                dto.sourceId());
        List<AlertConfigGroup> groups = alertConfigDao.findAssociationsByConfigIds(List.of(config.getId()));
        return AlertConfigDto.toDto(config, groups);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlertConfigDto updateConfig(Long id, UpdateAlertConfigDto dto) {
        // Validate recipient groups exist
        validateGroupCodes(dto.recipientGroupCodes());

        AlertConfig config = alertConfigDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + id));

        dto.updateEntity(config, objectMapper);
        alertConfigDao.update(config);

        // Xóa và tạo lại group associations
        alertConfigDao.deleteGroupAssociations(id);
        saveGroupAssociations(config, dto.recipientGroupCodes());

        List<AlertConfigGroup> groups = alertConfigDao.findAssociationsByConfigIds(List.of(id));
        return AlertConfigDto.toDto(config, groups);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        AlertConfig config = alertConfigDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + id));
        alertConfigDao.delete(config);
        log.info("[AlertConfig] Deleted config id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllBySource(AlertNamespace namespace, String sourceId) {
        int deleted = alertConfigDao.deleteAllByNamespaceAndSourceId(namespace, sourceId);
        log.info("[AlertConfig] Deleted {} configs for namespace={} sourceId={}", deleted, namespace, sourceId);
    }

    // ===== Private helpers =====

    private void validateGroupCodes(List<String> groupCodes) {
        if (groupCodes == null || groupCodes.isEmpty()) {
            return;
        }
        for (String code : groupCodes) {
            sysGroupDao.findEntityByCode(code)
                    .orElseThrow(() -> new BadRequestException("Recipient group not found: " + code));
        }
    }

    private void validateSourceId(AlertNamespace namespace, String sourceId) {
        if (namespace == null || sourceId == null || sourceId.isBlank()) {
            throw new BadRequestException("Namespace and source ID are required");
        }
        if (namespace == AlertNamespace.RULE) {
            try {
                Long ruleId = Long.parseLong(sourceId);
                ruleDao.findById(ruleId)
                        .orElseThrow(() -> new BadRequestException("Linked Rule ID does not exist: " + ruleId));
            } catch (NumberFormatException e) {
                throw new BadRequestException("Source ID for RULE namespace must be a valid numeric Rule ID");
            }
        } else if (namespace == AlertNamespace.GATEWAY) {
            try {
                Long gatewayId = Long.parseLong(sourceId);
                clientDao.findGatewayById(gatewayId).orElseThrow(
                        () -> new BadRequestException("Linked Gateway Client ID does not exist: " + gatewayId));
            } catch (NumberFormatException e) {
                throw new BadRequestException("Source ID for GATEWAY namespace must be a valid numeric Gateway ID");
            }
        }
    }

    private void saveGroupAssociations(AlertConfig config, List<String> groupCodes) {
        if (groupCodes == null || groupCodes.isEmpty()) return;
        for (String code : groupCodes) {
            SysGroup group = sysGroupDao.findEntityByCode(code)
                    .orElseThrow(() -> new NotFoundException("Group not found: " + code));
            alertConfigDao.saveGroupAssociation(config, group);
        }
    }
}
