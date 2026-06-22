package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.AlertConfigGroup;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.Severity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record AlertConfigDto(

    Long id,

    AlertNamespace namespace,

    String alertCode,

    String sourceId,

    String alertName,

    Severity severity,

    List<String> recipientGroupCodes,

    List<String> channels,

    String messageTemplate,

    Integer cooldownMinutes,

    Instant createdAt,

    Instant updatedAt

) {
  public static AlertConfigDto from(AlertConfig config, List<String> recipientGroupCodes, List<String> channels) {
    return new AlertConfigDto(config.getId(), config.getNamespace(), config.getAlertCode(), config.getSourceId(),
        config.getAlertName(), config.getSeverity(), recipientGroupCodes, channels, config.getMessageTemplate(),
        config.getCooldownMinutes(), config.getCreatedAt(), config.getUpdatedAt());
  }

    public static AlertConfigDto toDto(AlertConfig config, List<AlertConfigGroup> groups) {
        List<String> recipientGroupCodes = groups != null
                ? groups.stream().map(acg -> acg.getGroup().getGroupCode()).toList()
                : List.of();

        List<String> channels = new ArrayList<>();
        if (config.getChannels() != null && config.getChannels().isArray()) {
            config.getChannels().forEach(node -> channels.add(node.asText()));
        }
        return from(config, recipientGroupCodes, channels);
    }

    public static List<AlertConfigDto> toDtos(List<AlertConfig> configs, List<AlertConfigGroup> groups) {
        if (configs == null || configs.isEmpty()) return List.of();
        Map<Long, List<AlertConfigGroup>> groupsMap = new HashMap<>();
        if (groups != null) {
            groupsMap = groups.stream()
                .collect(Collectors.groupingBy(
                        acg -> acg.getAlertConfig().getId()
                ));
        }

        final Map<Long, List<AlertConfigGroup>> finalGroupsMap = groupsMap;
        return configs.stream().map(config -> {
            List<AlertConfigGroup> configGroups = finalGroupsMap.getOrDefault(config.getId(), List.of());
            return toDto(config, configGroups);
        }).toList();
    }
}
