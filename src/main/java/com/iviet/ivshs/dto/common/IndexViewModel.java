package com.iviet.ivshs.dto.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.lang.NonNull;

import com.iviet.ivshs.dto.floor.FloorDto;
import com.iviet.ivshs.dto.room.RoomDto;

import lombok.Builder;
import lombok.Data;

public record IndexViewModel(List<FloorDto> floors, Map<Long, List<RoomDto>> floorRoomMap, Map<Long, RoomInfo> roomInfoMap, Long totalFloors, Long totalRooms, Long totalHardwares) {

  @Data
  @Builder
  public static class RoomInfo {
    private Long hardwareCount;
    private Double latestAvgTemperature;
    private Double latestSumWatt;
  }

  @NonNull
  public Map<String, Object> toModelAttributes() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("floors", floors);
    attributes.put("floorRoomMap", floorRoomMap);
    attributes.put("roomInfoMap", roomInfoMap);
    attributes.put("totalFloors", totalFloors);
    attributes.put("totalRooms", totalRooms);
    attributes.put("totalHardwares", totalHardwares);
    return attributes;
  }
}
