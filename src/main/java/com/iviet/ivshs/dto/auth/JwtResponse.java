package com.iviet.ivshs.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    private String type;
    private Long id;
    private String username;
    private String clientType;
    private String avatarUrl;
    private Date lastLoginAt;
    private List<String> groups;

    public static JwtResponse of(String token, String username, List<String> groups) {
        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .username(username)
                .groups(groups)
                .build();
    }

    public static JwtResponse of(String token, CustomUserDetails userDetails) {
        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .clientType(userDetails.getClientType() != null ? userDetails.getClientType().name() : null)
                .avatarUrl(userDetails.getAvatarUrl())
                .lastLoginAt(userDetails.getLastLoginAt())
                .groups(userDetails.getGroups())
                .build();
    }
}
