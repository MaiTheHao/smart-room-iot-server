package com.iviet.ivshs.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public abstract class EventTelemetryApplicationEvent extends ApplicationEvent {

    private final String naturalId;
    private final DeviceCategory category;
    private final JsonNode payload;
    private final Instant eventTimestamp;

    public EventTelemetryApplicationEvent(Object source, String naturalId, DeviceCategory category, JsonNode payload, Instant eventTimestamp) {
        super(source);
        this.naturalId = naturalId;
        this.category = category;
        this.payload = payload;
        this.eventTimestamp = eventTimestamp;
    }
}
