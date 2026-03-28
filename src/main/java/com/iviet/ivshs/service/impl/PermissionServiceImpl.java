package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.enumeration.SysFunctionEnum;
import com.iviet.ivshs.exception.domain.ForbiddenException;
import com.iviet.ivshs.service.PermissionService;
import com.iviet.ivshs.util.RequestContextUtil;
import com.iviet.ivshs.util.SecurityContextUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

	@Override
	public boolean canManageFloor() {
        if (!RequestContextUtil.isHttpRequest()) return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_FLOOR.getCode());
    }

	@Override
    public void requireManageFloor() {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canManageFloor()) {
            throw new ForbiddenException("Insufficient permissions to manage floors");
        }
        log.debug("User {} granted permission to manage floors", SecurityContextUtil.getCurrentUsername());
    }

	@Override
    public boolean canManageRoom() {
        if (!RequestContextUtil.isHttpRequest()) return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasAllPermissions(List.of(
                        SysFunctionEnum.F_MANAGE_FLOOR.getCode(),
                        SysFunctionEnum.F_MANAGE_ROOM.getCode()
                ));
    }

	@Override
    public void requireManageRoom() {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canManageRoom()) {
            throw new ForbiddenException("Insufficient permissions to manage rooms");
        }
        log.debug("User {} granted permission to manage rooms", SecurityContextUtil.getCurrentUsername());
    }

	@Override
    public boolean canManageDevice() {
        if (!RequestContextUtil.isHttpRequest()) return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_DEVICE.getCode());
    }

	@Override
    public void requireManageDevice() {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canManageDevice()) {
            throw new ForbiddenException("Insufficient permissions to manage devices");
        }
        log.debug("User {} granted permission to manage devices", SecurityContextUtil.getCurrentUsername());
    }

	@Override
    public boolean canManageClient() {
        if (!RequestContextUtil.isHttpRequest()) return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_CLIENT.getCode());
    }

	@Override
    public void requireManageClient() {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canManageClient()) {
            throw new ForbiddenException("Insufficient permissions to manage clients");
        }
        log.debug("User {} granted permission to manage clients", SecurityContextUtil.getCurrentUsername());
    }

    @Override
    public boolean canManageSome() {
        if (!RequestContextUtil.isHttpRequest()) return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_SOME.getCode());
    }

    @Override
    public void requireManageSome() {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canManageSome()) {
            throw new ForbiddenException("Insufficient permissions to manage some resources");
        }
        log.debug("User {} granted permission to manage some resources", SecurityContextUtil.getCurrentUsername());
    }

    @Override
    public boolean canManageFunction() {
        if (!RequestContextUtil.isHttpRequest()) return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_FUNCTION.getCode());
    }

    @Override
    public void requireManageFunction() {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canManageFunction()) {
            throw new ForbiddenException("Insufficient permissions to manage functions");
        }
        log.debug("User {} granted permission to manage functions", SecurityContextUtil.getCurrentUsername());
    }

    @Override
    public boolean canManageGroup() {
        if (!RequestContextUtil.isHttpRequest()) return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_GROUP.getCode());
    }

    @Override
    public void requireManageGroup() {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canManageGroup()) {
            throw new ForbiddenException("Insufficient permissions to manage groups");
        }
        log.debug("User {} granted permission to manage groups", SecurityContextUtil.getCurrentUsername());
    }

    @Override
    public boolean canManageAutomation() {
        if (!RequestContextUtil.isHttpRequest()) return true;

        return SecurityContextUtil.hasPermission(MANAGE_ALL_PERMISSION) ||
                SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_AUTOMATION.getCode());
    }

    @Override
    public void requireManageAutomation() {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canManageAutomation()) {
            throw new ForbiddenException("Insufficient permissions to manage automation");
        }
        log.debug("User {} granted permission to manage automation", SecurityContextUtil.getCurrentUsername());
    }

	@Override
    public boolean canAccessFloor(String floorCode) {
        if (!RequestContextUtil.isHttpRequest()) return true;
        if (floorCode == null || floorCode.isBlank()) return false;

        return SecurityContextUtil.hasFloorAccess(floorCode);
    }

	@Override
    public void requireAccessFloor(String floorCode) {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canAccessFloor(floorCode)) {
            throw new ForbiddenException("Access to floor '" + floorCode + "' is denied");
        }
        log.debug("User {} granted access to floor '{}'", SecurityContextUtil.getCurrentUsername(), floorCode);
    }

	@Override
    public boolean canAccessRoom(String roomCode) {
        if (!RequestContextUtil.isHttpRequest()) return true;
        if (roomCode == null || roomCode.isBlank()) return false;

        return SecurityContextUtil.hasRoomAccess(roomCode);
    }

	@Override
    public void requireAccessRoom(String roomCode) {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!canAccessRoom(roomCode)) {
            throw new ForbiddenException("Access to room '" + roomCode + "' is denied");
        }
        log.debug("User {} granted access to room '{}'", SecurityContextUtil.getCurrentUsername(), roomCode);
    }

	@Override
    public Set<String> getAccessibleFloorCodes() {
        if (!RequestContextUtil.isHttpRequest()) return Set.of(ACCESS_ALL);

        return SecurityContextUtil.getAccessibleFloorCodes();
    }

	@Override
    public Set<String> getAccessibleRoomCodes() {
        if (!RequestContextUtil.isHttpRequest()) return Set.of(ACCESS_ALL);

        return SecurityContextUtil.getAccessibleRoomCodes();
    }

	@Override
    public boolean hasAccessToAllFloors(Set<String> floorCodes) {
        if (floorCodes == null || floorCodes.isEmpty() || !RequestContextUtil.isHttpRequest()) return true;

        Set<String> accessible = getAccessibleFloorCodes();
        return accessible.contains(ACCESS_ALL) || accessible.containsAll(floorCodes);
    }

	@Override
    public boolean hasAccessToAllRooms(Set<String> roomCodes) {
        if (roomCodes == null || roomCodes.isEmpty() || !RequestContextUtil.isHttpRequest()) return true;

        Set<String> accessible = getAccessibleRoomCodes();
        return accessible.contains(ACCESS_ALL) || accessible.containsAll(roomCodes);
    }

	@Override
    public boolean hasPermission(String functionCode) {
        if (!RequestContextUtil.isHttpRequest()) return true;
        if (functionCode == null || functionCode.isBlank()) return false;

        return SecurityContextUtil.hasPermission(functionCode);
    }

	@Override
    public boolean hasAllPermissions(List<String> functionCodes) {
        if (!RequestContextUtil.isHttpRequest()) return true;
        if (functionCodes == null || functionCodes.isEmpty()) return false;

        return SecurityContextUtil.hasAllPermissions(functionCodes);
    }

	@Override
    public boolean hasAnyPermission(List<String> functionCodes) {
        if (!RequestContextUtil.isHttpRequest()) return true;
        if (functionCodes == null || functionCodes.isEmpty()) return false;

        return SecurityContextUtil.hasAnyPermission(functionCodes);
    }

	@Override
    public void requirePermission(String functionCode, String message) {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!hasPermission(functionCode)) {
            throw new ForbiddenException(message != null ? message : "Permission denied");
        }
        log.debug("User {} granted permission '{}'", SecurityContextUtil.getCurrentUsername(), functionCode);
    }

	@Override
    public void requireAllPermissions(List<String> functionCodes, String message) {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!hasAllPermissions(functionCodes)) {
            throw new ForbiddenException(message != null ? message : "Insufficient permissions");
        }
        log.debug("User {} granted all permissions", SecurityContextUtil.getCurrentUsername());
    }

	@Override
    public void requireAnyPermission(List<String> functionCodes, String message) {
        if (!RequestContextUtil.isHttpRequest()) return;

        if (!hasAnyPermission(functionCodes)) {
            throw new ForbiddenException(message != null ? message : "Insufficient permissions");
        }
        log.debug("User {} granted one of required permissions", SecurityContextUtil.getCurrentUsername());
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
        if (!RequestContextUtil.isHttpRequest()) return Set.of();

        return SecurityContextUtil.getCurrentFunctions();
    }
}
