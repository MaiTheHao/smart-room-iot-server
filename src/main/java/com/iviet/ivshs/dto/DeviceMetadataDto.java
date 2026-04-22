package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.DeviceCategory;

public record DeviceMetadataDto(
	Long id,
	String naturalId,
	String name,
	String description,
	Boolean isActive,
	Long roomId,
	DeviceCategory category,
	Object device
) {
	public static DeviceMetadataDto from(
			Long id,
			String naturalId,
			String name,
			String description,
			Boolean isActive,
			Long roomId,
			DeviceCategory category,
			Object device
	) {
			return new DeviceMetadataDto(id, naturalId, name, description, isActive, roomId, category, device);
	}
}