package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.dto.TokenPayload;
import com.iviet.ivshs.shared.enumeration.TokenType;

public interface TokenStrategy<T extends TokenPayload> {

    TokenType getTokenType();

    String generateToken(T payload);

    T parseToken(String token);

    boolean validateToken(String token);
}
