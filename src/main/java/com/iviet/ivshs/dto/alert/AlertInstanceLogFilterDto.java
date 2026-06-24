package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;

public record AlertInstanceLogFilterDto(AlertActionType actionType, AlertActorType actorType, int page, int size) {
    public AlertInstanceLogFilterDto {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
    }
}
