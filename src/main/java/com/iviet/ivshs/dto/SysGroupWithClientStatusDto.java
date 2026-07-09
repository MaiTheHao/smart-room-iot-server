package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record SysGroupWithClientStatusDto(Long id, String groupCode, String name, String description, Boolean isAssignedToClient) {
    public static String jpqlProjection(String groupAlias, String groupLangAlias, String clientAlias) {
        return "%s.id, %s.groupCode, %s.name, %s.description, CASE WHEN %s.id IS NOT NULL THEN true ELSE false END"
            .formatted(groupAlias, groupAlias, groupLangAlias, groupLangAlias, clientAlias);
    }
}
