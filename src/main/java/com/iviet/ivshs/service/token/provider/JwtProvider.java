package com.iviet.ivshs.service.token.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.dto.token.TokenPayload;
import com.iviet.ivshs.shared.exception.InvalidTokenException;
import com.iviet.ivshs.shared.exception.TokenExpiredException;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtProvider {

    private final Map<String, Key> keys = new ConcurrentHashMap<>();

    private Key getKey(String secretKey) {
        Key key = keys.get(secretKey);
        if (key == null) {
            synchronized (this) {
                key = keys.get(secretKey);
                if (key == null) {
                    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
                    key = Keys.hmacShaKeyFor(keyBytes);
                    keys.put(secretKey, key);
                }
            }
        }
        return key;
    }

    public String generateToken(TokenPayload payload, String secretKey) {
        Map<String, Object> claims = new HashMap<>(payload.getClaims());

        if (payload.getExp() != null) {
            claims.put(Claims.EXPIRATION, payload.getExp().getEpochSecond());
        }
        if (payload.getIat() != null) {
            claims.put(Claims.ISSUED_AT, payload.getIat().getEpochSecond());
        }

        return Jwts.builder().setClaims(claims).signWith(getKey(secretKey), SignatureAlgorithm.HS512).compact();
    }

    public Claims parseToken(String token, String secretKey) {
        try {
            return Jwts.parserBuilder().setSigningKey(getKey(secretKey)).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("JWT token is expired", e);
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    public boolean validateToken(String token, String secretKey) {
        try {
            parseToken(token, secretKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
