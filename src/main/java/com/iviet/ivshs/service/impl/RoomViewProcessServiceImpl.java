package com.iviet.ivshs.service.impl;

import java.util.List;

// import org.springframework.cache.annotation.CacheEvict;
// import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.dto.RoomDto;
// import com.iviet.ivshs.enumeration.CacheDefinition;
import com.iviet.ivshs.service.LightService;
import com.iviet.ivshs.service.RoomService;
import com.iviet.ivshs.service.RoomViewProcessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomViewProcessServiceImpl implements RoomViewProcessService {

	private final RoomService roomService;
	private final LightService lightService;

	@Override
	// @Cacheable(
	// 	value = CacheDefinition._ROOM_VIEW_ROOM_METADATA,
	// 	key = "#a0 + T(com.iviet.ivshs.util.LocalContextUtil).getCurrentLangCodeFromRequest() + T(com.iviet.ivshs.util.SecurityContextUtil).getCurrentUsername()"
	// )
	public RoomDto getRoomMetadata(Long roomId) {
		return roomService.getById(roomId);
	}

	@Override
	// @Cacheable(
	// 	value = CacheDefinition._ROOM_VIEW_ROOM_LIGHTS,
	// 	key = "#a0 + T(com.iviet.ivshs.util.LocalContextUtil).getCurrentLangCodeFromRequest() + T(com.iviet.ivshs.util.SecurityContextUtil).getCurrentUsername()"
	// )
	public List<LightDto> getLightsForRoom(Long roomId) {
		return lightService.getListByRoomId(roomId, 0, 1000).content();
	}

	@Override
	// @CacheEvict(allEntries = true, value = {
	// 	CacheDefinition._ROOM_VIEW_ROOM_METADATA,
	// 	CacheDefinition._ROOM_VIEW_ROOM_LIGHTS
	// })
	public void evictAllCaches() {}
}
