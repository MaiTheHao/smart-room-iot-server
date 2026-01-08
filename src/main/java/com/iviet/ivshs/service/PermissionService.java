package com.iviet.ivshs.service;

import java.util.List;
import java.util.Set;

public interface PermissionService {

    public static final String ACCESS_ALL = "ALL";

    boolean hasPermission(Long clientId, String functionCode);

    boolean hasPermissions(Long clientId, List<String> functionCodes);

    Set<String> getPermissions(Long clientId);

    long countPermissions(Long clientId);

    void checkAccessToFloor(Long clientId, String floorCode);

    void checkAccessToRoom(Long clientId, String roomCode);
    
    Set<String> getAccessFloorCodes(Long clientId);

    Set<String> getAccessRoomCodes(Long clientId);
}
