package com.iviet.ivshs.shared.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class FunctionCodeHelper {

    public static final String FLOOR_PREFIX = "F_ACCESS_FLOOR_";
    public static final String ROOM_PREFIX = "F_ACCESS_ROOM_";
    public static final String MANAGE_PREFIX = "F_MANAGE_";
    public static final String GROUP_PREFIX = "G_";

    private static final Pattern MANAGE_PATTERN = Pattern
            .compile("^F_MANAGE_(CLIENT|FLOOR|ROOM|DEVICE|FUNCTION|GROUP|AUTOMATION|RULE|ALL|SOME)$");
    private static final Pattern ACCESS_PATTERN = Pattern.compile("^F_ACCESS_(FLOOR|ROOM)_([A-Za-z0-9_\\-]+|ALL)$");
    private static final Pattern GROUP_PATTERN = Pattern.compile("^G_[A-Z0-9_]+$");

    // --- Build Methods ---

    public static String buildFloorAccessCode(String floorCode) {
        return buildWithPrefix(floorCode, FLOOR_PREFIX, "Floor code");
    }

    public static String buildRoomAccessCode(String roomCode) {
        return buildWithPrefix(roomCode, ROOM_PREFIX, "Room code");
    }

    public static String buildManageCode(String domain) {
        return buildWithPrefix(domain, MANAGE_PREFIX, "Domain");
    }

    public static String buildGroupCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Group code cannot be null or blank");
        }
        String upper = code.toUpperCase();
        return upper.startsWith(GROUP_PREFIX) ? upper : GROUP_PREFIX + upper;
    }

    private static String buildWithPrefix(String value, String prefix, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return prefix + value;
    }

    // --- Extraction Methods ---

    public static String extractFloorCode(String functionCode) {
        return extractCode(functionCode, FLOOR_PREFIX);
    }

    public static String extractRoomCode(String functionCode) {
        return extractCode(functionCode, ROOM_PREFIX);
    }

    private static String extractCode(String functionCode, String prefix) {
        if (functionCode != null && functionCode.startsWith(prefix)) {
            return functionCode.substring(prefix.length());
        }
        return null;
    }

    public static Set<String> extractFloorCodes(Collection<String> functionCodes) {
        return extractCodes(functionCodes, FLOOR_PREFIX);
    }

    public static Set<String> extractRoomCodes(Collection<String> functionCodes) {
        return extractCodes(functionCodes, ROOM_PREFIX);
    }

    private static Set<String> extractCodes(Collection<String> functionCodes, String prefix) {
        if (functionCodes == null)
            return Set.of();
        return functionCodes.stream()
                .filter(code -> code != null && code.startsWith(prefix))
                .map(code -> code.substring(prefix.length()))
                .collect(Collectors.toSet());
    }

    // --- Validation Methods ---

    public static boolean isFloorAccessCode(String functionCode) {
        return functionCode != null && functionCode.startsWith(FLOOR_PREFIX);
    }

    public static boolean isRoomAccessCode(String functionCode) {
        return functionCode != null && functionCode.startsWith(ROOM_PREFIX);
    }

    public static boolean isValidFunctionCode(String functionCode) {
        if (functionCode == null)
            return false;
        return MANAGE_PATTERN.matcher(functionCode).matches() || ACCESS_PATTERN.matcher(functionCode).matches();
    }

    public static boolean isValidGroupCode(String groupCode) {
        if (groupCode == null)
            return false;
        return GROUP_PATTERN.matcher(groupCode).matches();
    }
}