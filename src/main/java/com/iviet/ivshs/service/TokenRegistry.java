package com.iviet.ivshs.service.token;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.shared.enumeration.TokenType;

@Component
public class TokenRegistry {

    private final Map<TokenType, TokenStrategy<?>> strategies;

    public TokenRegistry(List<TokenStrategy<?>> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(TokenStrategy::getTokenType, Function.identity()));
    }

    public Optional<TokenStrategy<?>> getStrategy(TokenType type) {
        return Optional.ofNullable(strategies.get(type));
    }

    public TokenStrategy<?> getStrategyOrThrow(TokenType type) {
        return getStrategy(type)
                .orElseThrow(() -> new IllegalArgumentException("No token strategy found for type: " + type));
    }
}
