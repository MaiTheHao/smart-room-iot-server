package com.iviet.ivshs.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
@Deprecated
public enum CacheDefinition {

	HOME_VIEW_FLOOR_ROOMS_MAP(CacheDefinition._HOME_VIEW_FLOOR_ROOMS_MAP, 5, TimeUnit.MINUTES, 100),
	HOME_VIEW_FLOORS_MAP(CacheDefinition._HOME_VIEW_FLOORS_MAP, 5, TimeUnit.MINUTES, 100),
	HOME_VIEW_ROOM_GATEWAY_COUNT(CacheDefinition._HOME_VIEW_ROOM_GATEWAY_COUNT, 1, TimeUnit.MINUTES, 500),
	HOME_VIEW_ROOM_LASTEST_TEMP(CacheDefinition._HOME_VIEW_ROOM_LASTEST_TEMP, 30, TimeUnit.SECONDS, 500),
	HOME_VIEW_ROOM_LASTEST_POWER(CacheDefinition._HOME_VIEW_ROOM_LASTEST_POWER, 30, TimeUnit.SECONDS, 500),
	
	ROOM_VIEW_ROOM_METADATA(CacheDefinition._ROOM_VIEW_ROOM_METADATA, 5, TimeUnit.MINUTES, 500),
	ROOM_VIEW_ROOM_LIGHTS(CacheDefinition._ROOM_VIEW_ROOM_LIGHTS, 5, TimeUnit.MINUTES, 500);

	// Phải tách riêng vì trong Annotation yêu cầu phải là hằng static final
	public static final String _HOME_VIEW_FLOOR_ROOMS_MAP = "home-view-floor-rooms-map";
	public static final String _HOME_VIEW_FLOORS_MAP = "home-view-floors-map";
	public static final String _HOME_VIEW_ROOM_GATEWAY_COUNT = "home-view-room-gateway-count";
	public static final String _HOME_VIEW_ROOM_LASTEST_TEMP = "home-view-room-lastest-temp";
	public static final String _HOME_VIEW_ROOM_LASTEST_POWER = "home-view-room-lastest-power";
	
	public static final String _ROOM_VIEW_ROOM_METADATA = "room-view-room-metadata";
	public static final String _ROOM_VIEW_ROOM_LIGHTS = "room-view-room-lights";

	private final String cacheName;
	private final long ttl;
	private final TimeUnit unit;
	private final long maxSize;
}
