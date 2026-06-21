package com.iviet.ivshs.service.notification.strategy.impl;

import com.iviet.ivshs.dto.notification.NotificationRequest;
import com.iviet.ivshs.service.notification.strategy.NotificationStrategy;
import com.iviet.ivshs.shared.enumeration.NotificationChannel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    }
}
