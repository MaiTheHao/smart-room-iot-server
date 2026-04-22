package com.iviet.ivshs.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.service.FanService;
import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.service.AirConditionService;
import com.iviet.ivshs.service.DeviceMetadataService;
import com.iviet.ivshs.service.LightService;
import com.iviet.ivshs.dao.DeviceMetadataDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceMetadataServiceImpl implements DeviceMetadataService {

  private final DeviceMetadataDao deviceMetadataDao;
  private final LightService lightService;
  private final FanService fanService;
  private final AirConditionService airConditionService;


  @Override
  public List<Object> getAllByRoomId(Long roomId, DeviceCategory category) {
    java.util.concurrent.CompletableFuture<List<LightDto>> lightFuture = (category == null || category == DeviceCategory.LIGHT)
        ? java.util.concurrent.CompletableFuture.supplyAsync(() -> lightService.getAllByRoomId(roomId))
        : java.util.concurrent.CompletableFuture.completedFuture(Collections.emptyList());

    java.util.concurrent.CompletableFuture<List<FanDto>> fanFuture = (category == null || category == DeviceCategory.FAN)
        ? java.util.concurrent.CompletableFuture.supplyAsync(() -> fanService.getAllByRoomId(roomId))
        : java.util.concurrent.CompletableFuture.completedFuture(Collections.emptyList());

    java.util.concurrent.CompletableFuture<List<AirConditionDto>> acFuture = (category == null || category == DeviceCategory.AIR_CONDITION)
        ? java.util.concurrent.CompletableFuture.supplyAsync(() -> airConditionService.getAllByRoomId(roomId))
        : java.util.concurrent.CompletableFuture.completedFuture(Collections.emptyList());

    return java.util.concurrent.CompletableFuture.allOf(lightFuture, fanFuture, acFuture)
        .thenApply(v -> {
          List<Object> all = new ArrayList<>();
          all.addAll(lightFuture.join());
          all.addAll(fanFuture.join());
          all.addAll(acFuture.join());
          return all;
        }).join();
  }

  @Override
  public Long getCountByRoomId(Long roomId) {
    return deviceMetadataDao.countByRoomId(roomId);
  }


}
