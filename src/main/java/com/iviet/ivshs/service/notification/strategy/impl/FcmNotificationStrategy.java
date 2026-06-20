package com.iviet.ivshs.service.notification.strategy.impl;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.ClientDevice;
import com.iviet.ivshs.service.notification.FcmDispatchService;
import com.iviet.ivshs.service.notification.channel.NotificationChannel;
import com.iviet.ivshs.service.notification.request.NotificationRequest;
import com.iviet.ivshs.service.notification.strategy.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FCM Push Notification Strategy.
 * Gom tất cả FCM tokens từ clientDevices của recipients rồi gửi multicast qua FcmDispatchService.
 * FcmDispatchService.sendToMultipleDevices() đã là @Async — không block thread hiện tại.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmNotificationStrategy implements NotificationStrategy {

    private final FcmDispatchService fcmDispatchService;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(NotificationRequest request) {
        List<String> fcmTokens = request.getRecipients().stream()
                .flatMap(client -> client.getClientDevices().stream())
                .map(ClientDevice::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .distinct()
                .collect(Collectors.toList());

        if (fcmTokens.isEmpty()) {
            log.debug("[FCM] No FCM tokens found for {} recipients — skipping", request.getRecipients().size());
            return;
        }

        log.info("[FCM] Dispatching push notification to {} tokens from {} recipients",
                fcmTokens.size(), request.getRecipients().size());
        // FcmDispatchService là @Async — không block thread này
        fcmDispatchService.sendToMultipleDevices(fcmTokens, request.getTitle(), request.getBody(), request.getData());
    }
}
