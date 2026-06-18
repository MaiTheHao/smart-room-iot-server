package com.iviet.ivshs.shared.util;

import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.entities.base.BaseIoTDevice;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;
import java.util.Map;
import java.util.Set;

public final class DeviceCapabilityRegistry {

	private record DeviceTypeKey(DeviceCategory category, DeviceSpecificType specificType) {
	}

	private static final Map<DeviceTypeKey, Set<String>> REGISTRY = Map.of(
			new DeviceTypeKey(DeviceCategory.FAN, DeviceSpecificType.GPIO),
			Set.of("power"),

			new DeviceTypeKey(DeviceCategory.FAN, DeviceSpecificType.IRSEND),
			Set.of("power", "speed", "mode", "swing"),

			new DeviceTypeKey(DeviceCategory.FAN, DeviceSpecificType.IR_CTL),
			Set.of("power", "speed", "mode", "swing"),

			new DeviceTypeKey(DeviceCategory.LIGHT, DeviceSpecificType.GPIO),
			Set.of("power"),

			new DeviceTypeKey(DeviceCategory.LIGHT, DeviceSpecificType.IRSEND),
			Set.of("power"),

			new DeviceTypeKey(DeviceCategory.LIGHT, DeviceSpecificType.IR_CTL),
			Set.of("power"));

	private DeviceCapabilityRegistry() {}

	public static boolean isSupported(DeviceCategory category, DeviceSpecificType specificType, String property) {
		if (category == DeviceCategory.AIR_CONDITION) {
			return true;
		}
		DeviceTypeKey key = new DeviceTypeKey(category, specificType);
		Set<String> supported = REGISTRY.get(key);
		if (supported == null) {
			return false;
		}
		return supported.contains(property);
	}

	public static boolean isSupported(BaseIoTDevice<?> device, String property) {
		if (device == null) {
			return false;
		}
		DeviceCategory category = null;
		if (device instanceof Fan) {
			category = DeviceCategory.FAN;
		} else if (device instanceof Light) {
			category = DeviceCategory.LIGHT;
		} else if (device instanceof AirCondition) {
			category = DeviceCategory.AIR_CONDITION;
		}
		if (category == null) {
			return false;
		}
		return isSupported(category, device.getSpecificType(), property);
	}
}
