package com.iviet.ivshs.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.dto.RoomDto;
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
	public RoomDto getRoomMetadata(Long roomId) {
		return roomService.getById(roomId);
	}

	@Override
	public List<LightDto> getLightsForRoom(Long roomId) {
		return lightService.getListByRoomId(roomId, 0, 1000).content();
	}

	@Override
	public void evictAllCaches() {
		log.debug("[ROOM-VIEW-PROCESS] Evicting all caches for room view");
	}
}