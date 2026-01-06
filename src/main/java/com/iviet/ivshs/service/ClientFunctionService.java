package com.iviet.ivshs.service;

public interface ClientFunctionService {

    int rebuildCacheForClient(Long clientId);

    int rebuildCacheForGroup(Long groupId);

    int rebuildAllCache();

    int clearCacheForClientGroup(Long clientId, Long groupId);

    int clearCacheForClient(Long clientId);

    int clearCacheForGroup(Long groupId);

    int clearCacheForFunction(String functionCode);

    int addPermissionsForClientGroup(Long clientId, Long groupId);

    int addPermissionsForGroupFunction(Long groupId, String functionCode);

    boolean validateCache(Long clientId);
}
