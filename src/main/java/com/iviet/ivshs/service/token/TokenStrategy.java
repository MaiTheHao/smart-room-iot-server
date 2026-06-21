package com.iviet.ivshs.service.token;

import com.iviet.ivshs.dto.token.TokenPayload;
import com.iviet.ivshs.shared.enumeration.TokenType;

public interface TokenStrategy<T extends TokenPayload> {

    TokenType getTokenType();

    String generateToken(T payload);

    T parseToken(String token);

    boolean validateToken(String token);
}
