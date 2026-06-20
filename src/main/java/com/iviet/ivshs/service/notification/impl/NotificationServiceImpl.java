package com.iviet.ivshs.service.notification.impl;

import com.iviet.ivshs.service.notification.NotificationService;
import com.iviet.ivshs.service.notification.channel.NotificationChannel;
import com.iviet.ivshs.service.notification.request.NotificationRequest;
import com.iviet.ivshs.service.notification.strategy.NotificationStrategyRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Central dispatcher cho tất cả kênh thông báo sử dụng NotificationStrategyRegistry.
 * Class này tuân thủ Open/Closed Principle (OCP) vì khi thêm kênh mới,
 * ta chỉ cần tạo một component Strategy mới mà không cần chỉnh sửa class này.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationStrategyRegistry registry;

    @Override
    public void sendNotification(NotificationRequest request) {
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            log.debug("[Notification] No recipients — skipping all channels");
            return;
        }
        if (request.getChannels() == null || request.getChannels().isEmpty()) {
            log.debug("[Notification] No channels configured — skipping");
            return;
        }

        for (NotificationChannel channel : request.getChannels()) {
            registry.findStrategy(channel).ifPresentOrElse(
                    strategy -> {
                        log.debug("[Notification] Dispatching via '{}' to {} recipients",
                                channel, request.getRecipients().size());
                        try {
                            strategy.send(request);
                        } catch (Exception e) {
                            log.error("[Notification] Failed to send notification via channel '{}': {}",
                                    channel, e.getMessage(), e);
                        }
                    },
                    () -> log.warn("[Notification] No strategy registered for channel '{}'. Skipping.", channel)
            );
        }
    }
}
