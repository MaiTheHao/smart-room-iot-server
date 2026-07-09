package com.iviet.ivshs.dto;

/**
 * Record DTO representing the mapping between a Group ID and a Function Code.
 */
public record GroupPermissionMapping(Long groupId, String functionCode) {
    public static String jpqlProjection(String groupAlias, String funcAlias) {
        return "%s.id, %s.functionCode"
            .formatted(groupAlias, funcAlias);
    }
}
