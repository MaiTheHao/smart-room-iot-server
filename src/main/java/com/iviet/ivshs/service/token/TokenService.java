package com.iviet.ivshs.service.token;

import com.iviet.ivshs.dto.TokenPayload;
import com.iviet.ivshs.shared.enumeration.TokenType;

public interface TokenService {
    String generateToken(TokenType type, TokenPayload payload);

    TokenPayload parseToken(TokenType type, String token);

    boolean validateToken(TokenType type, String token);
}
