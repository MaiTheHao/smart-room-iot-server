package com.iviet.ivshs.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public abstract class RoomEventApplicationEvent extends ApplicationEvent {

    private final Long roomId;
    private final RoomEventCode eventCode;
    private final JsonNode payload;
    private final Instant eventTimestamp;

    public RoomEventApplicationEvent(Object source, Long roomId, RoomEventCode eventCode, JsonNode payload, Instant eventTimestamp) {
        super(source);
        this.roomId = roomId;
        this.eventCode = eventCode;
        this.payload = payload;
        this.eventTimestamp = eventTimestamp;
    }
}
