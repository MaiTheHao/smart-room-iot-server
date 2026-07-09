package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dto.NotificationRequest;
import com.iviet.ivshs.service.NotificationService;
import com.iviet.ivshs.service.strategy.NotificationStrategyRegistry;
import com.iviet.ivshs.shared.enumeration.NotificationChannel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationStrategyRegistry registry;

    @Async
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
            registry.findStrategy(channel).ifPresentOrElse(strategy -> {
                log.debug("[Notification] Dispatching via '{}' to {} recipients", channel,
                        request.getRecipients().size());
                try {
                    strategy.send(request);
                } catch (Exception e) {
                    log.error("[Notification] Failed to send notification via channel '{}': {}", channel,
                            e.getMessage(), e);
                }
            }, () -> log.warn("[Notification] No strategy registered for channel '{}'. Skipping.", channel));
        }
    }
}
