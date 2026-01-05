package com.iviet.ivshs.service;

import java.util.List;
import java.util.Set;

public interface PermissionServiceV1 {

    boolean hasPermission(Long clientId, String functionCode);

    boolean hasPermissions(Long clientId, List<String> functionCodes);

    Set<String> getPermissions(Long clientId);

    long countPermissions(Long clientId);
}
