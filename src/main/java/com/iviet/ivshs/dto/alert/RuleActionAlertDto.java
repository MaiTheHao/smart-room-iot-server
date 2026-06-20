package com.iviet.ivshs.dto.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.RuleActionAlert;
import com.iviet.ivshs.shared.enumeration.Severity;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public record RuleActionAlertDto(
        Long id,
        Long ruleId,
        String alertName,
        Severity severity,
        List<String> recipientGroups,
        List<String> channels,
        String messageTemplate,
        Integer cooldownMinutes,
        Boolean autoResolve
) {
    public static RuleActionAlertDto from(RuleActionAlert entity) {
        if (entity == null) {
            return null;
        }
        return RuleActionAlertDto.builder()
                .id(entity.getId())
                .ruleId(entity.getRule() != null ? entity.getRule().getId() : null)
                .alertName(entity.getAlertName())
                .severity(entity.getSeverity())
                .recipientGroups(parseJsonArray(entity.getRecipientGroups()))
                .channels(parseJsonArray(entity.getChannels()))
                .messageTemplate(entity.getMessageTemplate())
                .cooldownMinutes(entity.getCooldownMinutes())
                .autoResolve(entity.getAutoResolve())
                .build();
    }

    private static List<String> parseJsonArray(JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.isArray()) {
            return List.of();
        }
        List<String> list = new ArrayList<>();
        jsonNode.forEach(node -> {
            String text = node.asText();
            if (text != null && !text.isBlank()) {
                list.add(text);
            }
        });
        return list;
    }
}
