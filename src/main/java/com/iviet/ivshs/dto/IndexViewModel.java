package com.iviet.ivshs.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.lang.NonNull;

import lombok.Builder;
import lombok.Data;

/**
 * DashboardViewModel contains summary information for the dashboard.
 * - Map of floors to their respective rooms.
 * - Detailed information for each room.
 * - Cumulative statistics: total floors, rooms, and hardwares.
 */
public record IndexViewModel(
  Map<FloorDto, List<RoomDto>> floorRoomMap,
  Map<Long, RoomInfo> roomInfoMap,
  Long totalFloors,
  Long totalRooms,
  Long totalHardwares
) {

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
    attributes.put("floorRoomMap", (floorRoomMap != null ? floorRoomMap : Map.of()));
    attributes.put("roomInfoMap", (roomInfoMap != null ? roomInfoMap : Map.of()));
    attributes.put("totalFloors", (totalFloors != null ? totalFloors : 0L));
    attributes.put("totalRooms", (totalRooms != null ? totalRooms : 0L));
    attributes.put("totalHardwares", (totalHardwares != null ? totalHardwares : 0L));
    return attributes;
  }
}
