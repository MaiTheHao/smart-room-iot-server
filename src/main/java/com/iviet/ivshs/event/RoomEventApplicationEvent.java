package com.iviet.ivshs.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import lombok.Getter;

import java.time.Instant;

@Getter
public abstract class RoomEventApplicationEvent extends EventTelemetryApplicationEvent {

    private final Long roomId;
    private final RoomEventCode eventCode;

    public RoomEventApplicationEvent(Object source, String naturalId, DeviceCategory category, Long roomId, RoomEventCode eventCode, JsonNode payload, Instant eventTimestamp) {
        super(source, naturalId, category, payload, eventTimestamp);
        this.roomId = roomId;
        this.eventCode = eventCode;
    }
}
