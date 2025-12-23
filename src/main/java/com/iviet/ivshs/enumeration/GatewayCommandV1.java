package com.iviet.ivshs.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatewayCommandV1 {

	ON("true"),
	OFF("false"),
	GET("get"),
	HEALTH_CHECK("check"),
	LEVEL("level"); // For dimmable devices (lights, fans) - requires additional level value

	private final String value;

	/**
	 * Create a level command with the specified intensity value.
	 * @param level the intensity level (0-100)
	 * @return the command string in format "level:XX"
	 */
	public static String levelCommand(int level) {
		if (level < 0 || level > 100) {
			throw new IllegalArgumentException("Level must be between 0 and 100");
		}
		return LEVEL.getValue() + ":" + level;
	}

	@Override
	public String toString() {
		return value;
	}
}
