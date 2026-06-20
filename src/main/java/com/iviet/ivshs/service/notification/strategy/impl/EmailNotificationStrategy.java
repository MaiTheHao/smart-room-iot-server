package com.iviet.ivshs.service.notification.strategy.impl;

import com.iviet.ivshs.service.notification.channel.NotificationChannel;
import com.iviet.ivshs.service.notification.request.NotificationRequest;
import com.iviet.ivshs.service.notification.strategy.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Email Notification Strategy — Placeholder.
 * Tích hợp JavaMailSender sẽ được hoàn thiện trong sprint tiếp theo.
 * Log để xác nhận luồng chạy đúng mà không throw exception.
 * Kênh PUSH vẫn hoạt động bình thường dù EMAIL chưa implement.
 */
@Slf4j
@Component
public class EmailNotificationStrategy implements NotificationStrategy {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationRequest request) {
        log.info("[EMAIL][PLACEHOLDER] Would send email '{}' to {} recipients. JavaMailSender not yet integrated.",
                request.getTitle(), request.getRecipients().size());
        // TODO Sprint N+1: inject JavaMailSender, build MimeMessage, send per recipient email.
    }
}
