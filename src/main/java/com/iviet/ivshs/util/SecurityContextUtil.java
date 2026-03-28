package com.iviet.ivshs.util;

import com.iviet.ivshs.dto.CustomUserDetails;
import com.iviet.ivshs.enumeration.SysFunctionEnum;
import com.iviet.ivshs.exception.domain.ForbiddenException;
import com.iviet.ivshs.exception.domain.UnauthorizedException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class SecurityContextUtil {

    public Long getCurrentClientId() {
        return getCustomUserDetails().getId();
    }

    public String getCurrentUsername() {
        return getCustomUserDetails().getUsername();
    }

    public Set<String> getCurrentFunctions() {
        return getCustomUserDetails().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public boolean hasPermission(String functionCode) {
        if (functionCode == null || functionCode.isBlank()) return false;
        return getCurrentFunctions().contains(functionCode);
    }

    public boolean hasAllPermissions(Collection<String> functionCodes) {
        if (functionCodes == null || functionCodes.isEmpty()) return false;
        Set<String> userFunctions = getCurrentFunctions();
        return userFunctions.containsAll(functionCodes);
    }

    public boolean hasAnyPermission(Collection<String> functionCodes) {
        if (functionCodes == null || functionCodes.isEmpty()) return false;
        Set<String> userFunctions = getCurrentFunctions();
        return functionCodes.stream().anyMatch(userFunctions::contains);
    }

    public void requirePermission(String functionCode, String forbiddenMessage) {
        if (!hasPermission(functionCode)) {
            throw new ForbiddenException(forbiddenMessage);
        }
    }

    public void requireAllPermissions(Collection<String> functionCodes, String forbiddenMessage) {
        if (!hasAllPermissions(functionCodes)) {
            throw new ForbiddenException(forbiddenMessage);
        }
    }

    public void requireAnyPermission(Collection<String> functionCodes, String forbiddenMessage) {
        if (!hasAnyPermission(functionCodes)) {
            throw new ForbiddenException(forbiddenMessage);
        }
    }

    public boolean hasFloorAccess(String floorCode) {
        if (hasPermission(SysFunctionEnum.F_ACCESS_FLOOR_ALL.getCode())) {
            return true;
        }
        String specificFunc = FunctionCodeHelper.buildFloorAccessCode(floorCode);
        return hasPermission(specificFunc);
    }

    public boolean hasRoomAccess(String roomCode) {
        if (hasPermission(SysFunctionEnum.F_ACCESS_ROOM_ALL.getCode())) {
            return true;
        }
        String specificFunc = FunctionCodeHelper.buildRoomAccessCode(roomCode);
        return hasPermission(specificFunc);
    }

    public void requireFloorAccess(String floorCode) {
        if (!hasFloorAccess(floorCode)) {
            throw new ForbiddenException("Access to floor " + floorCode + " is denied");
        }
    }

    public void requireRoomAccess(String roomCode) {
        if (!hasRoomAccess(roomCode)) {
            throw new ForbiddenException("Access to room " + roomCode + " is denied");
        }
    }

    public Set<String> getAccessibleFloorCodes() {
        Set<String> functions = getCurrentFunctions();
        
        if (functions.contains(SysFunctionEnum.F_ACCESS_FLOOR_ALL.getCode())) {
            return Set.of("ALL");
        }
        
        return FunctionCodeHelper.extractFloorCodes(functions);
    }

    public Set<String> getAccessibleRoomCodes() {
        Set<String> functions = getCurrentFunctions();
        
        if (functions.contains(SysFunctionEnum.F_ACCESS_ROOM_ALL.getCode())) {
            return Set.of("ALL");
        }
        
        return FunctionCodeHelper.extractRoomCodes(functions);
    }

    private CustomUserDetails getCustomUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        
        log.error("Principal is not an instance of CustomUserDetails");
        throw new UnauthorizedException("Invalid user session");
    }
}