package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.dto.NotificationRequest;
import com.iviet.ivshs.service.strategy.NotificationStrategy;
import com.iviet.ivshs.shared.enumeration.NotificationChannel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SMS Notification Strategy — Placeholder. Tích hợp SMS Gateway (Twilio, VietGuys...) trong sprint tiếp theo.
 */
@Slf4j
@Component
public class SmsNotificationStrategy implements NotificationStrategy {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(NotificationRequest request) {
        log.info("[SMS][PLACEHOLDER] Would send SMS '{}' to {} recipients. SMS Gateway not yet integrated.",
                request.getBody(), request.getRecipients().size());
        // TODO Sprint N+1: inject SmsGatewayClient, send body per recipient phone number.
    }
}
