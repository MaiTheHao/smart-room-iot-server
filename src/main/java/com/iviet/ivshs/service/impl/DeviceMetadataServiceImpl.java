package com.iviet.ivshs.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.service.FanService;
import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.dto.DeviceMetadataDto;
import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.service.AirConditionService;
import com.iviet.ivshs.service.DeviceMetadataService;
import com.iviet.ivshs.service.LightService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceMetadataServiceImpl implements DeviceMetadataService {

  private final LightService lightService;
  private final AirConditionService airConditionService;
  private final FanService fanService;

  @Override
  public List<DeviceMetadataDto> getAll() {
    List<DeviceMetadataDto> devices = new ArrayList<>();

    // Get Lights
    List<LightDto> lights = lightService.getAll();
    if (lights != null) {
      devices.addAll(lights.stream().map(this::mapLightToMetadata).toList());
    }

    // Get Air Conditions
    List<AirConditionDto> airConditions = airConditionService.getAll();
    if (airConditions != null) {
      devices.addAll(airConditions.stream().map(this::mapAirConditionToMetadata).toList());
    }

    // Get Fan
      List<FanDto> fan = fanService.getAll();
      if (fan != null) {
          devices.addAll(fan.stream().map(this::mapFanToMetadata).toList());
      }

    return devices;
  }

  @Override
  public List<DeviceMetadataDto> getAllByRoomId(Long roomId) {
    List<DeviceMetadataDto> devices = new ArrayList<>();

    // Get Lights by Room
    List<LightDto> lights = lightService.getAllByRoomId(roomId);
    if (lights != null) {
      devices.addAll(lights.stream().map(this::mapLightToMetadata).toList());
    }

    // Get Air Conditions by Room
    List<AirConditionDto> airConditions = airConditionService.getAllByRoomId(roomId);
    if (airConditions != null) {
      devices.addAll(airConditions.stream().map(this::mapAirConditionToMetadata).toList());
    }

      // Get Fan by Room
      List<FanDto> fan = fanService.getAllByRoomId(roomId);
      if (fan != null) {
          devices.addAll(fan.stream().map(this::mapFanToMetadata).toList());
      }

    return devices;
  }

  @Override
  public List<DeviceMetadataDto> getAllByClientId(Long clientId) {
    // Currently not supported/implemented for underlying services in this hardcoded version
    return Collections.emptyList();
  }

  private DeviceMetadataDto mapLightToMetadata(LightDto light) {
    return DeviceMetadataDto.from(
        light.id(),
        light.naturalId(),
        light.name(),
        light.description(),
        light.isActive(),
        light.roomId(),
        DeviceCategory.LIGHT
    );
  }

  private DeviceMetadataDto mapAirConditionToMetadata(AirConditionDto ac) {
    return DeviceMetadataDto.from(
        ac.id(),
        ac.naturalId(),
        ac.name(),
        ac.description(),
        ac.isActive(),
        ac.roomId(),
        DeviceCategory.AIR_CONDITION
    );
  }
    private DeviceMetadataDto mapFanToMetadata(FanDto fan) {
        return DeviceMetadataDto.from(
                fan.id(),
                fan.naturalId(),
                fan.name(),
                fan.description(),
                fan.isActive(),
                fan.roomId(),
                DeviceCategory.FAN
        );
    }
}
