package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.FloorDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.enumeration.SysFunctionEnum;
import com.iviet.ivshs.exception.domain.ForbiddenException;
import com.iviet.ivshs.service.PermissionService;
import com.iviet.ivshs.util.RequestContextUtil;
import com.iviet.ivshs.util.SecurityContextUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Slf4j
@Service("permissionService")
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final FloorDao floorDao;
    private final RoomDao roomDao;

    @Override
    public boolean canManageFloor() {
        if (!RequestContextUtil.isHttpRequest())
            return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_FLOOR.getCode());
    }

    @Override
    public void requireManageFloor() {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canManageFloor();
        logDecision("MANAGE_FLOOR", null, allowed, allowed ? "Granted" : "Insufficient permissions to manage floors");
        if (!allowed) {
            throw new ForbiddenException("Insufficient permissions to manage floors");
        }
    }

    @Override
    public boolean canManageRoom() {
        if (!RequestContextUtil.isHttpRequest())
            return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasAllPermissions(List.of(
                        SysFunctionEnum.F_MANAGE_FLOOR.getCode(),
                        SysFunctionEnum.F_MANAGE_ROOM.getCode()));
    }

    @Override
    public void requireManageRoom() {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canManageRoom();
        logDecision("MANAGE_ROOM", null, allowed, allowed ? "Granted" : "Insufficient permissions to manage rooms");
        if (!allowed) {
            throw new ForbiddenException("Insufficient permissions to manage rooms");
        }
    }

    @Override
    public boolean canManageDevice() {
        if (!RequestContextUtil.isHttpRequest())
            return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_DEVICE.getCode());
    }

    @Override
    public void requireManageDevice() {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canManageDevice();
        logDecision("MANAGE_DEVICE", null, allowed, allowed ? "Granted" : "Insufficient permissions to manage devices");
        if (!allowed) {
            throw new ForbiddenException("Insufficient permissions to manage devices");
        }
    }

    @Override
    public boolean canManageClient() {
        if (!RequestContextUtil.isHttpRequest())
            return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_CLIENT.getCode());
    }

    @Override
    public void requireManageClient() {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canManageClient();
        logDecision("MANAGE_CLIENT", null, allowed, allowed ? "Granted" : "Insufficient permissions to manage clients");
        if (!allowed) {
            throw new ForbiddenException("Insufficient permissions to manage clients");
        }
    }

    @Override
    public boolean canManageSome() {
        if (!RequestContextUtil.isHttpRequest())
            return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_SOME.getCode());
    }

    @Override
    public void requireManageSome() {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canManageSome();
        logDecision("MANAGE_SOME", null, allowed,
                allowed ? "Granted" : "Insufficient permissions to manage some resources");
        if (!allowed) {
            throw new ForbiddenException("Insufficient permissions to manage some resources");
        }
    }

    @Override
    public boolean canManageFunction() {
        if (!RequestContextUtil.isHttpRequest())
            return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_FUNCTION.getCode());
    }

    @Override
    public void requireManageFunction() {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canManageFunction();
        logDecision("MANAGE_FUNCTION", null, allowed,
                allowed ? "Granted" : "Insufficient permissions to manage functions");
        if (!allowed) {
            throw new ForbiddenException("Insufficient permissions to manage functions");
        }
    }

    @Override
    public boolean canManageGroup() {
        if (!RequestContextUtil.isHttpRequest())
            return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_GROUP.getCode());
    }

    @Override
    public void requireManageGroup() {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canManageGroup();
        logDecision("MANAGE_GROUP", null, allowed, allowed ? "Granted" : "Insufficient permissions to manage groups");
        if (!allowed) {
            throw new ForbiddenException("Insufficient permissions to manage groups");
        }
    }

    @Override
    public boolean canManageAutomation() {
        if (!RequestContextUtil.isHttpRequest())
            return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_AUTOMATION.getCode());
    }

    @Override
    public void requireManageAutomation() {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canManageAutomation();
        logDecision("MANAGE_AUTOMATION", null, allowed,
                allowed ? "Granted" : "Insufficient permissions to manage automation");
        if (!allowed) {
            throw new ForbiddenException("Insufficient permissions to manage automation");
        }
    }

    @Override
    public boolean canManageRole() {
        if (!RequestContextUtil.isHttpRequest())
            return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_ROLE.getCode());
    }

    @Override
    public void requireManageRole() {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canManageRole();
        logDecision("MANAGE_ROLE", null, allowed, allowed ? "Granted" : "Insufficient permissions to manage roles");
        if (!allowed) {
            throw new ForbiddenException("Insufficient permissions to manage roles");
        }
    }

    @Override
    public boolean canAccessFloor(String floorCode) {
        if (!RequestContextUtil.isHttpRequest())
            return true;
        if (floorCode == null || floorCode.isBlank())
            return false;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasFloorAccess(floorCode);
    }

    @Override
    public void requireAccessFloor(String floorCode) {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canAccessFloor(floorCode);
        logDecision("ACCESS_FLOOR", floorCode, allowed,
                allowed ? "Granted" : "Access to floor '" + floorCode + "' is denied");
        if (!allowed) {
            throw new ForbiddenException("Access to floor '" + floorCode + "' is denied");
        }
    }

    @Override
    public boolean canAccessRoom(String roomCode) {
        if (!RequestContextUtil.isHttpRequest())
            return true;
        if (roomCode == null || roomCode.isBlank())
            return false;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasRoomAccess(roomCode);
    }

    @Override
    public void requireAccessRoom(String roomCode) {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canAccessRoom(roomCode);
        logDecision("ACCESS_ROOM", roomCode, allowed,
                allowed ? "Granted" : "Access to room '" + roomCode + "' is denied");
        if (!allowed) {
            throw new ForbiddenException("Access to room '" + roomCode + "' is denied");
        }
    }

    @Override
    public boolean canAccessFloor(Long id) {
        if (!RequestContextUtil.isHttpRequest())
            return true;
        if (hasPermission(MANAGE_ALL_PERMISSION))
            return true;
        if (id == null)
            return false;

        return floorDao.findById(id).map(f -> canAccessFloor(f.getCode())).orElse(false);
    }

    @Override
    public void requireAccessFloor(Long id) {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canAccessFloor(id);
        logDecision("ACCESS_FLOOR_BY_ID", id, allowed,
                allowed ? "Granted" : "Access to floor with ID " + id + " is denied");
        if (!allowed) {
            throw new ForbiddenException("Access to floor with ID " + id + " is denied");
        }
    }

    @Override
    public boolean canAccessRoom(Long id) {
        if (!RequestContextUtil.isHttpRequest())
            return true;
        if (hasPermission(MANAGE_ALL_PERMISSION))
            return true;
        if (id == null)
            return false;

        return roomDao.findById(id).map(r -> canAccessRoom(r.getCode())).orElse(false);
    }

    @Override
    public void requireAccessRoom(Long id) {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = canAccessRoom(id);
        logDecision("ACCESS_ROOM_BY_ID", id, allowed,
                allowed ? "Granted" : "Access to room with ID " + id + " is denied");
        if (!allowed) {
            throw new ForbiddenException("Access to room with ID " + id + " is denied");
        }
    }

    @Override
    public Set<String> getAccessibleFloorCodes() {
        if (!RequestContextUtil.isHttpRequest())
            return Set.of(ACCESS_ALL);

        return SecurityContextUtil.getAccessibleFloorCodes();
    }

    @Override
    public Set<String> getAccessibleRoomCodes() {
        if (!RequestContextUtil.isHttpRequest())
            return Set.of(ACCESS_ALL);

        return SecurityContextUtil.getAccessibleRoomCodes();
    }

    @Override
    public boolean hasAccessToAllFloors(Set<String> floorCodes) {
        if (floorCodes == null || floorCodes.isEmpty() || !RequestContextUtil.isHttpRequest())
            return true;

        Set<String> accessible = getAccessibleFloorCodes();
        return accessible.contains(ACCESS_ALL) || accessible.containsAll(floorCodes);
    }

    @Override
    public boolean hasAccessToAllRooms(Set<String> roomCodes) {
        if (roomCodes == null || roomCodes.isEmpty() || !RequestContextUtil.isHttpRequest())
            return true;

        Set<String> accessible = getAccessibleRoomCodes();
        return accessible.contains(ACCESS_ALL) || accessible.containsAll(roomCodes);
    }

    @Override
    public boolean hasPermission(String functionCode) {
        if (!RequestContextUtil.isHttpRequest())
            return true;
        if (functionCode == null || functionCode.isBlank())
            return false;

        return SecurityContextUtil.hasPermission(functionCode);
    }

    @Override
    public boolean hasAllPermissions(List<String> functionCodes) {
        if (!RequestContextUtil.isHttpRequest())
            return true;
        if (functionCodes == null || functionCodes.isEmpty())
            return false;

        return SecurityContextUtil.hasAllPermissions(functionCodes);
    }

    @Override
    public boolean hasAnyPermission(List<String> functionCodes) {
        if (!RequestContextUtil.isHttpRequest())
            return true;
        if (functionCodes == null || functionCodes.isEmpty())
            return false;

        return SecurityContextUtil.hasAnyPermission(functionCodes);
    }

    @Override
    public void requirePermission(String functionCode, String message) {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = hasPermission(functionCode);
        String reason = allowed ? "Granted" : (message != null ? message : "Permission denied");
        logDecision("REQUIRE_PERMISSION", functionCode, allowed, reason);
        if (!allowed) {
            throw new ForbiddenException(reason);
        }
    }

    @Override
    public void requireAllPermissions(List<String> functionCodes, String message) {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = hasAllPermissions(functionCodes);
        String reason = allowed ? "Granted" : (message != null ? message : "Insufficient permissions");
        logDecision("REQUIRE_ALL_PERMISSIONS", functionCodes, allowed, reason);
        if (!allowed) {
            throw new ForbiddenException(reason);
        }
    }

    @Override
    public void requireAnyPermission(List<String> functionCodes, String message) {
        if (!RequestContextUtil.isHttpRequest())
            return;

        boolean allowed = hasAnyPermission(functionCodes);
        String reason = allowed ? "Granted" : (message != null ? message : "Insufficient permissions");
        logDecision("REQUIRE_ANY_PERMISSION", functionCodes, allowed, reason);
        if (!allowed) {
            throw new ForbiddenException(reason);
        }
    }

    @Override
    public Long getCurrentUserId() {
        return SecurityContextUtil.getCurrentClientId();
    }

    @Override
    public String getCurrentUsername() {
        return SecurityContextUtil.getCurrentUsername();
    }

    @Override
    public Set<String> getCurrentPermissions() {
        if (!RequestContextUtil.isHttpRequest())
            return Set.of();

        return SecurityContextUtil.getCurrentFunctions();
    }

    private void logDecision(String action, Object target, boolean result, String reason) {
        if (!log.isDebugEnabled())
            return;

        log.debug(
                "user='{}' action='{}' target='{}' result={} reason='{}'",
                SecurityContextUtil.getCurrentUsername(),
                action,
                target,
                result,
                reason);
    }
}
