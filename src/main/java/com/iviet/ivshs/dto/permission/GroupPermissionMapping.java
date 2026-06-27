package com.iviet.ivshs.dto.permission;

/**
 * Record DTO representing the mapping between a Group ID and a Function Code.
 */
public record GroupPermissionMapping(Long groupId, String functionCode) {}
