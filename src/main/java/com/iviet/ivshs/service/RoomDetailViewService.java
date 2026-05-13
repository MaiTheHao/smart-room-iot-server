package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.RoomDetailViewModel;

import lombok.Builder;
import lombok.Data;

public interface RoomDetailViewService {

  public RoomDetailViewModel getModel(GetModelCriteria req);

  @Data
  @Builder
  public static class GetModelCriteria {
    private Long roomId;
  }
}
