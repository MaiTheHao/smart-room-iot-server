package com.iviet.ivshs.service.registry;

import com.iviet.ivshs.service.strategy.NotificationStrategy;
import com.iviet.ivshs.shared.enumeration.NotificationChannel;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class NotificationStrategyRegistry {

  private final Map<NotificationChannel, NotificationStrategy> registry;

  public NotificationStrategyRegistry(List<NotificationStrategy> strategyList) {
    Map<NotificationChannel, NotificationStrategy> tempRegistry =
        new EnumMap<>(NotificationChannel.class);

    for (NotificationStrategy strategy : strategyList) {
      NotificationChannel channel = strategy.getChannel();
      NotificationStrategy existing = tempRegistry.put(channel, strategy);

      if (existing != null) {
        throw new IllegalStateException("Duplicate strategy detected for channel '" + channel
            + "': "
            + existing.getClass().getSimpleName() + " vs " + strategy.getClass().getSimpleName()
            + ". Only one NotificationStrategy per channel is allowed.");
      }
    }

    this.registry = Collections.unmodifiableMap(tempRegistry);
  }

  public Optional<NotificationStrategy> findStrategy(NotificationChannel channel) {
    return Optional.ofNullable(registry.get(channel));
  }

  public boolean hasStrategy(NotificationChannel channel) {
    return registry.containsKey(channel);
  }
}
