package com.iviet.ivshs.service.notification;

import com.iviet.ivshs.dto.NotificationRequest;

public interface NotificationService {

    void sendNotification(NotificationRequest request);
}
