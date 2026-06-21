package com.iviet.ivshs.service.notification.strategy;

import com.iviet.ivshs.dto.notification.NotificationRequest;
import com.iviet.ivshs.shared.enumeration.NotificationChannel;

public interface NotificationStrategy {

    NotificationChannel getChannel();

    void send(NotificationRequest request);
}
