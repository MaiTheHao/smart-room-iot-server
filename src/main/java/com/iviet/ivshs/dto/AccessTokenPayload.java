package com.iviet.ivshs.dto;

import java.util.Map;
import java.time.Instant;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import com.iviet.ivshs.shared.enumeration.ClientType;

@Getter
@Setter
@SuperBuilder
public class AccessTokenPayload extends TokenPayload {
    private String username;
    private ClientType clientType;
    private String avatarUrl;
    private Instant lastLoginAt;
    private Set<String> groups;

    public AccessTokenPayload() {
        super();
    }

    public AccessTokenPayload(String sub, Instant exp, Instant iat, String iss, String username, ClientType clientType,
            String avatarUrl, Instant lastLoginAt, Set<String> groups) {
        super(sub, exp, iat, iss);
        this.username = username;
        this.clientType = clientType;
        this.avatarUrl = avatarUrl;
        this.lastLoginAt = lastLoginAt;
        this.groups = groups;
    }

    @Override
    public Map<String, Object> getClaims() {
        Map<String, Object> claims = super.getClaims();
        if (username != null) claims.put("username", username);
        if (clientType != null) claims.put("clientType", clientType.name());
        if (avatarUrl != null) claims.put("avatarUrl", avatarUrl);
        if (lastLoginAt != null) claims.put("lastLoginAt", lastLoginAt);
        if (groups != null) claims.put("groups", groups);
        return claims;
    }

    @Override
    public void fromClaims(Map<String, Object> claims) {
        if (claims == null) return;
        super.fromClaims(claims);
        if (claims.get("username") != null) this.username = (String) claims.get("username");
        if (claims.get("clientType") != null) {
            Object ctVal = claims.get("clientType");
            if (ctVal instanceof ClientType) {
                this.clientType = (ClientType) ctVal;
            } else {
                this.clientType = ClientType.valueOf(ctVal.toString());
            }
        }
        if (claims.get("avatarUrl") != null) this.avatarUrl = (String) claims.get("avatarUrl");
        if (claims.get("lastLoginAt") != null) {
            this.lastLoginAt = parseInstant(claims.get("lastLoginAt"));
        }
        if (claims.get("groups") != null) {
            Object groupsVal = claims.get("groups");
            if (groupsVal instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> set = (Set<String>) groupsVal;
                this.groups = set;
            } else if (groupsVal instanceof java.util.Collection) {
                this.groups = new java.util.HashSet<>((java.util.Collection<String>) groupsVal);
            }
        }
    }
}
