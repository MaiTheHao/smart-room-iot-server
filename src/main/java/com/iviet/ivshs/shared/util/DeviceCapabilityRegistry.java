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
			new DeviceTypeKey(DeviceCategory.FAN, DeviceSpecificType.GPIO), Set.of("power", "speed"),

			new DeviceTypeKey(DeviceCategory.FAN, DeviceSpecificType.IRSEND), Set.of("power", "speed", "mode", "swing"),

			new DeviceTypeKey(DeviceCategory.FAN, DeviceSpecificType.IR_CTL), Set.of("power", "speed", "mode", "swing"),

			new DeviceTypeKey(DeviceCategory.LIGHT, DeviceSpecificType.GPIO), Set.of("power", "level"),

			new DeviceTypeKey(DeviceCategory.LIGHT, DeviceSpecificType.IRSEND), Set.of("power", "level"),

			new DeviceTypeKey(DeviceCategory.LIGHT, DeviceSpecificType.IR_CTL), Set.of("power", "level"),

			new DeviceTypeKey(DeviceCategory.AIR_CONDITION, DeviceSpecificType.GPIO), Set.of("power"),

			new DeviceTypeKey(DeviceCategory.AIR_CONDITION, DeviceSpecificType.IRSEND),
			Set.of("power", "temperature", "mode", "fanSpeed", "swing"),

			new DeviceTypeKey(DeviceCategory.AIR_CONDITION, DeviceSpecificType.IR_CTL),
			Set.of("power", "temperature", "mode", "fanSpeed", "swing"));

	private DeviceCapabilityRegistry() {
	}

	public static boolean isSupported(DeviceCategory category, DeviceSpecificType specificType, String property) {
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

		DeviceCategory category = switch (device) {
		case Fan f -> DeviceCategory.FAN;
		case Light l -> DeviceCategory.LIGHT;
		case AirCondition ac -> DeviceCategory.AIR_CONDITION;
		default -> null;
		};

		if (category == null) {
			return false;
		}

		return isSupported(category, device.getSpecificType(), property);
	}

	public static Set<String> getCapabilities(DeviceCategory category, DeviceSpecificType specificType) {
		DeviceTypeKey key = new DeviceTypeKey(category, specificType);
		Set<String> supported = REGISTRY.get(key);
		return supported != null ? supported : Set.of();
	}

	public static Set<String> getCapabilities(BaseIoTDevice<?> device) {
		if (device == null) {
			return Set.of();
		}

		DeviceCategory category = switch (device) {
		case Fan f -> DeviceCategory.FAN;
		case Light l -> DeviceCategory.LIGHT;
		case AirCondition ac -> DeviceCategory.AIR_CONDITION;
		default -> null;
		};

		if (category == null) {
			return Set.of();
		}

		return getCapabilities(category, device.getSpecificType());
	}
}
