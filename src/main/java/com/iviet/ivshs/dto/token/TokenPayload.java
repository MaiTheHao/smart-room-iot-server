package com.iviet.ivshs.dto.token;

import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import com.iviet.ivshs.shared.constant.AppConstant;

@Getter
@Setter
@SuperBuilder
public abstract class TokenPayload {
    private String sub;
    private Instant exp;
    private Instant iat;

    @lombok.Builder.Default
    private String iss = AppConstant.TOKEN_ISSUER;

    protected TokenPayload() {
        this.iss = AppConstant.TOKEN_ISSUER;
    }

    protected TokenPayload(String sub, Instant exp, Instant iat, String iss) {
        this.sub = sub;
        this.exp = exp;
        this.iat = iat != null ? iat : Instant.now();
        this.iss = iss != null ? iss : AppConstant.TOKEN_ISSUER;
    }

    public Map<String, Object> getClaims() {
        Map<String, Object> claims = new HashMap<>();
        if (sub != null) claims.put("sub", sub);
        if (iss != null) claims.put("iss", iss);
        if (iat != null) claims.put("iat", iat);
        if (exp != null) claims.put("exp", exp);
        return claims;
    }

    protected Instant parseInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Instant) {
            return (Instant) value;
        } else if (value instanceof Number) {
            long val = ((Number) value).longValue();
            return Instant.ofEpochMilli(val < 100000000000L ? val * 1000 : val);
        } else if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant();
        }
        return null;
    }

    public void fromClaims(Map<String, Object> claims) {
        if (claims == null) return;
        if (claims.get("sub") != null) this.sub = (String) claims.get("sub");
        if (claims.get("iss") != null) this.iss = (String) claims.get("iss");
        if (claims.get("iat") != null) this.iat = parseInstant(claims.get("iat"));
        if (claims.get("exp") != null) this.exp = parseInstant(claims.get("exp"));
    }
}
