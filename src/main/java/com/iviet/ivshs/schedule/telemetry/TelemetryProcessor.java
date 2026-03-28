package com.iviet.ivshs.schedule.telemetry;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.service.RoomService;
import com.iviet.ivshs.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "TELEMETRY_PROCESSOR")
@Component
@RequiredArgsConstructor
public class TelemetryProcessor {

  private final TelemetryService telemetryService;
  private final RoomService roomService;

  public void processRoom(Long roomId) {
    if (roomId == null) {
      log.warn("Room ID is null");
      return;
    }

    log.info("Processing telemetry for room ID: {}", roomId);
    long start = System.currentTimeMillis();

    try {
      RoomDto room = roomService.getById(roomId);
      if (room == null) {
        log.warn("Room not found: {}", roomId);
        return;
      }

      telemetryService.takeByRoom(roomId);
      
      log.info("Completed telemetry processing for room {} in {}ms", 
        roomId, System.currentTimeMillis() - start);
    } catch (Exception e) {
      log.error("Failed to process telemetry for room {}: {}", roomId, e.getMessage(), e);
    }
  }

  public void processAllGateways() {
    log.info("Processing telemetry for all gateways");
    try {
      telemetryService.takeGlobalTelemetry();
    } catch (Exception e) {
      log.error("Failed to process gateway telemetry: {}", e.getMessage(), e);
    }
  }
}
