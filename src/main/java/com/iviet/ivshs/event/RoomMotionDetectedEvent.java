package com.iviet.ivshs.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import lombok.Getter;

import java.time.Instant;

@Getter
public class RoomMotionDetectedEvent extends RoomEventApplicationEvent {

    public RoomMotionDetectedEvent(Object source, String naturalId, Long roomId, JsonNode payload, Instant eventTimestamp) {
        super(source, naturalId, DeviceCategory.MOTION_DETECTOR, roomId, RoomEventCode.MOTION_DETECTED, payload, eventTimestamp);
    }
}
