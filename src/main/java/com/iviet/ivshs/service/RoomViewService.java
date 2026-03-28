package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.RoomDetailViewModel;

public interface RoomViewService {
	
	RoomDetailViewModel getRoomDetailModel(Long roomId, String startedAtStr, String endedAtStr);
}
