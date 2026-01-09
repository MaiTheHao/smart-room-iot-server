package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.dto.RoomDto;

public interface RoomViewProcessService {
	
	RoomDto getRoomMetadata(Long roomId);
	
	List<LightDto> getLightsForRoom(Long roomId);

	void evictAllCaches();
}
