package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.RoomDetailViewModel;

public interface RoomViewService {
	
	RoomDetailViewModel getModel(Long roomId, String startedAtStr, String endedAtStr);
	void refreshRoomDetailData();
}
