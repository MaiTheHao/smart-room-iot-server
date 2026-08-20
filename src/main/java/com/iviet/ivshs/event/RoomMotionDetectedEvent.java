package com.iviet.ivshs.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import lombok.Getter;

import java.time.Instant;

@Getter
public class RoomMotionDetectedEvent extends RoomEventApplicationEvent {

    public RoomMotionDetectedEvent(Object source, Long roomId, JsonNode payload, Instant eventTimestamp) {
        super(source, roomId, RoomEventCode.MOTION_DETECTED, payload, eventTimestamp);
    }
}
