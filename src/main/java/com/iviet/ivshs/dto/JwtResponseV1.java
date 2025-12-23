package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseV1 {
    
    private String token;
    private String type;
    private String username;
    private List<String> roles;

    public static JwtResponseV1 of(String token, String username, List<String> roles) {
        return JwtResponseV1.builder()
                .token(token)
                .type("Bearer")
                .username(username)
                .roles(roles)
                .build();
    }
}
