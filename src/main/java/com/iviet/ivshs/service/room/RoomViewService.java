package com.iviet.ivshs.service.room;

import com.iviet.ivshs.dto.room.RoomDetailViewModel;

import lombok.Builder;
import lombok.Data;

public interface RoomViewService {

  public RoomDetailViewModel getRoomDetailModel(RoomDetailCriteria req);

  @Data
  @Builder
  public static class RoomDetailCriteria {
    private Long roomId;
  }
}
