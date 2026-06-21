package com.iviet.ivshs.service.notification.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.shared.enumeration.NotificationChannel;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class NotificationStrategyRegistry {

    private final Map<NotificationChannel, NotificationStrategy> registry;

    public NotificationStrategyRegistry(List<NotificationStrategy> strategyList) {
        Map<NotificationChannel, NotificationStrategy> tempRegistry = new EnumMap<>(NotificationChannel.class);

        for (NotificationStrategy strategy : strategyList) {
            NotificationChannel channel = strategy.getChannel();
            NotificationStrategy existing = tempRegistry.put(channel, strategy);

            if (existing != null) {
                throw new IllegalStateException(
                        "[NotificationStrategyRegistry] Duplicate strategy detected for channel '" + channel + "': "
                                + existing.getClass().getSimpleName() + " vs " + strategy.getClass().getSimpleName()
                                + ". Only one NotificationStrategy per channel is allowed.");
            }

            log.info("[NotificationStrategyRegistry] Registered '{}' → {}", channel,
                    strategy.getClass().getSimpleName());
        }

        this.registry = Collections.unmodifiableMap(tempRegistry);

        log.info("[NotificationStrategyRegistry] Initialized with {} channels: {}", registry.size(), registry.keySet());
    }

    public Optional<NotificationStrategy> findStrategy(NotificationChannel channel) {
        return Optional.ofNullable(registry.get(channel));
    }

    public boolean hasStrategy(NotificationChannel channel) {
        return registry.containsKey(channel);
    }
}
