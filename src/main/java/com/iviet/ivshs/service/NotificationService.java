package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.NotificationRequest;

public interface NotificationService {

    void sendNotification(NotificationRequest request);
}
