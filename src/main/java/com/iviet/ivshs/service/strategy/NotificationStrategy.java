package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.dto.NotificationRequest;
import com.iviet.ivshs.shared.enumeration.NotificationChannel;

public interface NotificationStrategy {

    NotificationChannel getChannel();

    void send(NotificationRequest request);
}
