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
public class JwtResponse {
    
    private String token;
    private String type;
    private String username;
    private List<String> groups;

    public static JwtResponse of(String token, String username, List<String> groups) {
        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .username(username)
                .groups(groups)
                .build();
    }
}
