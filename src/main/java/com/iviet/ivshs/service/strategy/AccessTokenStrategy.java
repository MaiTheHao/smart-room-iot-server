package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.core.properties.TokenProperties;
import com.iviet.ivshs.dto.AccessTokenPayload;

import java.time.Instant;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.strategy.TokenStrategy;
import com.iviet.ivshs.service.impl.JwtProvider;
import com.iviet.ivshs.shared.enumeration.TokenType;

@Component
@RequiredArgsConstructor
public class AccessTokenStrategy implements TokenStrategy<AccessTokenPayload> {

    private final TokenProperties tokenProperties;
    private final JwtProvider jwtProvider;

    @Override
    public TokenType getTokenType() {
        return TokenType.ACCESS;
    }

    @Override
    public String generateToken(AccessTokenPayload payload) {
        payload.setExp(Instant.now().plusMillis(tokenProperties.getAccessExpMs()));
        return jwtProvider.generateToken(payload, tokenProperties.getAccessSecret());
    }

    @Override
    public AccessTokenPayload parseToken(String token) {
        Claims claims = jwtProvider.parseToken(token, tokenProperties.getAccessSecret());
        AccessTokenPayload payload = new AccessTokenPayload();
        payload.fromClaims(claims);
        return payload;
    }

    @Override
    public boolean validateToken(String token) {
        return jwtProvider.validateToken(token, tokenProperties.getAccessSecret());
    }
}
