package com.iviet.ivshs.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class FunctionCodeHelper {
    
    private static final String FLOOR_PREFIX = "F_ACCESS_FLOOR_";
    private static final String ROOM_PREFIX = "F_ACCESS_ROOM_";
    
    public static String buildFloorAccessCode(String floorCode) {
        if (floorCode == null || floorCode.isBlank()) {
            throw new IllegalArgumentException("Floor code cannot be null or blank");
        }
        return FLOOR_PREFIX + floorCode;
    }
    
    public static String buildRoomAccessCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new IllegalArgumentException("Room code cannot be null or blank");
        }
        return ROOM_PREFIX + roomCode;
    }
    
    public static String extractFloorCode(String functionCode) {
        if (isFloorAccessCode(functionCode)) {
            return functionCode.substring(FLOOR_PREFIX.length());
        }
        return null;
    }

    public static String extractRoomCode(String functionCode) {
        if (isRoomAccessCode(functionCode)) {
            return functionCode.substring(ROOM_PREFIX.length());
        }
        return null;
    }

    public static boolean isFloorAccessCode(String functionCode) {
        return functionCode != null && functionCode.startsWith(FLOOR_PREFIX);
    }

    public static boolean isRoomAccessCode(String functionCode) {
        return functionCode != null && functionCode.startsWith(ROOM_PREFIX);
    }

    public static Set<String> extractFloorCodes(Collection<String> functionCodes) {
        return functionCodes.stream()
                .filter(FunctionCodeHelper::isFloorAccessCode)
                .map(FunctionCodeHelper::extractFloorCode)
                .collect(Collectors.toSet());
    }

    public static Set<String> extractRoomCodes(Collection<String> functionCodes) {
        return functionCodes.stream()
                .filter(FunctionCodeHelper::isRoomAccessCode)
                .map(FunctionCodeHelper::extractRoomCode)
                .collect(Collectors.toSet());
    }
}
