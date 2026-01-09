package com.iviet.ivshs.service;

import com.iviet.ivshs.enumeration.SysFunctionEnum;
import java.util.List;
import java.util.Set;

public interface PermissionService {

    public static final String ACCESS_ALL = "ALL";
    public static final String MANAGE_ALL_PERMISSION = SysFunctionEnum.F_MANAGE_ALL.getCode();

    boolean canManageFloor();

    void requireManageFloor();

    boolean canManageRoom();

    void requireManageRoom();

    boolean canManageDevice();

    void requireManageDevice();

    boolean canManageClient();

    void requireManageClient();

    boolean canManageSome();

    void requireManageSome();

    boolean canManageFunction();

    void requireManageFunction();

    boolean canManageGroup();

    void requireManageGroup();

    boolean canAccessFloor(String floorCode);

    void requireAccessFloor(String floorCode);

    boolean canAccessRoom(String roomCode);

    void requireAccessRoom(String roomCode);

    Set<String> getAccessibleFloorCodes();

    Set<String> getAccessibleRoomCodes();

    boolean hasAccessToAllFloors(Set<String> floorCodes);

    boolean hasAccessToAllRooms(Set<String> roomCodes);

    boolean hasPermission(String functionCode);

    boolean hasAllPermissions(List<String> functionCodes);

    boolean hasAnyPermission(List<String> functionCodes);

    void requirePermission(String functionCode, String message);

    void requireAllPermissions(List<String> functionCodes, String message);

    void requireAnyPermission(List<String> functionCodes, String message);

    Long getCurrentUserId();

    String getCurrentUsername();

    Set<String> getCurrentPermissions();
}