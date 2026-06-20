package com.iviet.ivshs.service.notification;

import com.iviet.ivshs.service.notification.request.NotificationRequest;

/**
 * Facade interface cho việc gửi thông báo qua nhiều kênh.
 * AlertService (và các service khác) CHỈ phụ thuộc vào interface này, không biết cụ thể FCM/Email/SMS.
 * Implementation cụ thể là NotificationServiceImpl — sử dụng Registry để gửi thông báo.
 */
public interface NotificationService {

    /**
     * Gửi thông báo tới tập hợp recipients qua tất cả các channels được chỉ định.
     *
     * @param request Value Object chứa recipients, channels, title, body, extra data payload.
     */
    void sendNotification(NotificationRequest request);
}
