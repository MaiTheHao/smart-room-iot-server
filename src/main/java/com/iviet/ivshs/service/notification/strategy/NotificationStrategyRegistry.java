package com.iviet.ivshs.service.notification.strategy;

import com.iviet.ivshs.service.notification.channel.NotificationChannel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry chứa tất cả NotificationStrategy beans được inject bởi Spring.
 * Ánh xạ NotificationChannel → NotificationStrategy bằng EnumMap (O(1), memory-efficient).
 *
 * Lợi ích so với List<NotificationStrategy> trong NotificationServiceImpl cũ:
 * - Lookup O(1) thay vì O(n) linear scan qua list
 * - @PostConstruct fail-fast: ném exception ngay lúc startup nếu 2 strategy cùng channel,
 *   thay vì silent bug lúc runtime
 * - SRP: tách rời trách nhiệm "tìm strategy" ra khỏi NotificationServiceImpl
 *
 * NotificationServiceImpl chỉ cần inject Registry, không cần biết List cụ thể.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStrategyRegistry {

    /** Spring inject tất cả @Component implements NotificationStrategy vào list này. */
    private final List<NotificationStrategy> strategyList;

    /** Map được build một lần lúc startup, dùng EnumMap cho performance tối ưu. */
    private Map<NotificationChannel, NotificationStrategy> registry;

    /**
     * Build registry map từ strategyList.
     * Chạy NGAY SAU KHI Spring khởi tạo bean này.
     *
     * Fail-fast: nếu 2 strategy cùng channel, throw IllegalStateException tại startup
     * thay vì silent override hoặc lỗi lúc runtime.
     */
    @PostConstruct
    public void init() {
        registry = new EnumMap<>(NotificationChannel.class);

        for (NotificationStrategy strategy : strategyList) {
            NotificationChannel channel = strategy.getChannel();
            NotificationStrategy existing = registry.put(channel, strategy);

            if (existing != null) {
                throw new IllegalStateException(
                        "[NotificationStrategyRegistry] Duplicate strategy detected for channel '" + channel +
                        "': " + existing.getClass().getSimpleName() +
                        " vs " + strategy.getClass().getSimpleName() +
                        ". Only one NotificationStrategy per channel is allowed.");
            }

            log.info("[NotificationStrategyRegistry] Registered '{}' → {}",
                    channel, strategy.getClass().getSimpleName());
        }

        log.info("[NotificationStrategyRegistry] Initialized with {} channels: {}",
                registry.size(), registry.keySet());
    }

    /**
     * Tìm strategy theo channel. O(1) lookup qua EnumMap.
     *
     * @return Optional.empty() nếu không có strategy cho channel này.
     */
    public Optional<NotificationStrategy> findStrategy(NotificationChannel channel) {
        return Optional.ofNullable(registry.get(channel));
    }

    /** Kiểm tra channel có strategy hay không. */
    public boolean hasStrategy(NotificationChannel channel) {
        return registry.containsKey(channel);
    }
}
