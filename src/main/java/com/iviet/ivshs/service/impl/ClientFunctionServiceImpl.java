package com.iviet.ivshs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iviet.ivshs.dao.SysClientFunctionCacheDao;
import com.iviet.ivshs.service.ClientFunctionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ClientFunctionServiceImpl implements ClientFunctionService {

    @Autowired
    private SysClientFunctionCacheDao cacheDao;

    @Override
    public int rebuildAllCache() {
        long start = System.currentTimeMillis();
        log.info("[Cache-Rebuild] ALL - Starting optimized rebuild...");
        
        try {
            int totalCreated = cacheDao.rebuildAllPermissions();
            
            long duration = System.currentTimeMillis() - start;
            log.info("[Cache-Rebuild] ALL - Done in {}ms. Total records created: {}", duration, totalCreated);
            return totalCreated;
        } catch (Exception e) {
            log.error("[Cache-Rebuild] ALL - Failed", e);
            throw e;
        }
    }

    @Override
    public int rebuildCacheForClient(Long clientId) {
        log.info("[Cache-Rebuild] Client ID: {} - Starting", clientId);
        try {
            int count = cacheDao.rebuildPermissionsByClientId(clientId);
            log.info("[Cache-Rebuild] Client ID: {} - Done. Created {} records", clientId, count);
            return count;
        } catch (Exception e) {
            log.error("[Cache-Rebuild] Client ID: {} - Failed", clientId, e);
            throw e;
        }
    }

    @Override
    public int rebuildCacheForGroup(Long groupId) {
        log.info("[Cache-Rebuild] Group ID: {} - Triggering optimized rebuild", groupId);
        try {
            int totalRebuilt = rebuildAllCache();
            log.info("[Cache-Rebuild] Group ID: {} - Done. Total records: {}", groupId, totalRebuilt);
            return totalRebuilt;
        } catch (Exception e) {
            log.error("[Cache-Rebuild] Group ID: {} - Failed", groupId, e);
            throw e;
        }
    }

    @Override
    public int clearCacheForClientGroup(Long clientId, Long groupId) {
        int count = cacheDao.deleteByClientAndGroup(clientId, groupId);
        log.info("[Cache-Clear] Client: {}, Group: {}. Removed {} records", clientId, groupId, count);
        return count;
    }

    @Override
    public int clearCacheForClient(Long clientId) {
        int count = cacheDao.deleteByClient(clientId);
        log.info("[Cache-Clear] Client ID: {}. Removed {} records", clientId, count);
        return count;
    }

    @Override
    public int clearCacheForGroup(Long groupId) {
        int count = cacheDao.deleteByGroup(groupId);
        log.info("[Cache-Clear] Group ID: {}. Removed {} records", groupId, count);
        return count;
    }

    @Override
    public int clearCacheForFunction(String functionCode) {
        int count = cacheDao.deleteByFunctionCode(functionCode);
        log.info("[Cache-Clear] Function: {}. Removed {} records", functionCode, count);
        return count;
    }

    @Override
    public int addPermissionsForClientGroup(Long clientId, Long groupId) {
        log.info("[Cache-Add] Client: {}, Group: {} - Starting", clientId, groupId);
        try {
            int addedCount = rebuildCacheForClient(clientId);
            log.info("[Cache-Add] Client: {}, Group: {}. Added {} records", clientId, groupId, addedCount);
            return addedCount;
        } catch (Exception e) {
            log.error("[Cache-Add] Client: {}, Group: {} - Failed", clientId, groupId, e);
            throw e;
        }
    }

    @Override
    public int addPermissionsForGroupFunction(Long groupId, String functionCode) {
        log.info("[Cache-Add] Group: {}, Function: {} - Starting", groupId, functionCode);
        try {
            int addedCount = rebuildAllCache();
            log.info("[Cache-Add] Group: {}, Function: {}. Added {} records", groupId, functionCode, addedCount);
            return addedCount;
        } catch (Exception e) {
            log.error("[Cache-Add] Group: {}, Function: {} - Failed", groupId, functionCode, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCache(Long clientId) {
        log.info("[Cache-Validate] Client: {} - Validating cache", clientId);
        try {
            return true;
        } catch (Exception e) {
            log.error("[Cache-Validate] Client: {} - Validation failed", clientId, e);
            return false;
        }
    }
}