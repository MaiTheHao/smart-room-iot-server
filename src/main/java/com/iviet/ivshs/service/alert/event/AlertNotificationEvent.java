package com.iviet.ivshs.service.alert.event;

import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AlertNotificationEvent extends ApplicationEvent {
    private final AlertConfig config;
    private final AlertInstance alert;
    private final AlertActionType actionType;
    private final AlertActorType actorType;
    private final String actorId;

    public AlertNotificationEvent(Object source, AlertConfig config, AlertInstance alert, AlertActionType actionType,
            AlertActorType actorType, String actorId) {
        super(source);
        this.config = config;
        this.alert = alert;
        this.actionType = actionType;
        this.actorType = actorType;
        this.actorId = actorId;
    }
}
