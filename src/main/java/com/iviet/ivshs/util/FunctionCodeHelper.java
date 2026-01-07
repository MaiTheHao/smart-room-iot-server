package com.iviet.ivshs.util;

/**
 * Utility để parse và kiểm tra function code
 */
public class FunctionCodeHelper {
    
    // Pattern cho floor access và room access
    private static final String FLOOR_PREFIX = "F_ACCESS_FLOOR_";
    private static final String ROOM_PREFIX = "F_ACCESS_ROOM_";
    
    public static String buildFloorAccessCode(String floorCode) {
        return FLOOR_PREFIX + floorCode;
    }
    
    public static String buildRoomAccessCode(String roomCode) {
        return ROOM_PREFIX + roomCode;
    }
    
    public static String extractFloorCode(String functionCode) {
        if (functionCode.startsWith(FLOOR_PREFIX)) {
            return functionCode.substring(FLOOR_PREFIX.length());
        }
        return null;
    }

	public static String extractRoomCode(String functionCode) {
		if (functionCode.startsWith(ROOM_PREFIX)) {
			return functionCode.substring(ROOM_PREFIX.length());
		}		return null;
	}
}
