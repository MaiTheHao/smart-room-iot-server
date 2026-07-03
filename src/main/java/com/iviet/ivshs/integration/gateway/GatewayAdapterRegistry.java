package com.iviet.ivshs.integration.gateway;

import com.iviet.ivshs.shared.enumeration.ClientType;
import com.iviet.ivshs.shared.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GatewayAdapterRegistry {

    private final List<GatewayAdapter> adapters;
    private final Map<ClientType, GatewayAdapter> registry = new EnumMap<>(ClientType.class);

    public GatewayAdapterRegistry(List<GatewayAdapter> adapters) {
        this.adapters = adapters;
    }

    @PostConstruct
    private void init() {
        for (GatewayAdapter adapter : adapters) {
            GatewayAdapter previous = registry.put(adapter.getSupportedType(), adapter);
            if (previous != null) {
                log.warn("Duplicate GatewayAdapter for type [{}]: {} overrides {}",
                    adapter.getSupportedType(),
                    adapter.getClass().getSimpleName(),
                    previous.getClass().getSimpleName());
            }
        }
        log.info("GatewayAdapterRegistry initialized with {} adapters: {}",
            registry.size(), registry.keySet());
    }

    public GatewayAdapter get(ClientType type) {
        GatewayAdapter adapter = registry.get(type);
        if (adapter == null) {
            throw new BadRequestException(
                "No GatewayAdapter registered for type: " + type +
                ". Registered types: " + registry.keySet());
        }
        return adapter;
    }
}
