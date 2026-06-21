package com.iviet.ivshs.service.token.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.token.TokenPayload;
import com.iviet.ivshs.service.token.TokenRegistry;
import com.iviet.ivshs.service.token.TokenService;
import com.iviet.ivshs.service.token.TokenStrategy;
import com.iviet.ivshs.shared.enumeration.TokenType;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRegistry tokenRegistry;

    @Override
    @SuppressWarnings("unchecked")
    public String generateToken(TokenType type, TokenPayload payload) {
        TokenStrategy<TokenPayload> strategy = (TokenStrategy<TokenPayload>) tokenRegistry.getStrategyOrThrow(type);
        return strategy.generateToken(payload);
    }

    @Override
    public TokenPayload parseToken(TokenType type, String token) {
        return tokenRegistry.getStrategyOrThrow(type).parseToken(token);
    }

    @Override
    public boolean validateToken(TokenType type, String token) {
        return tokenRegistry.getStrategyOrThrow(type).validateToken(token);
    }
}
