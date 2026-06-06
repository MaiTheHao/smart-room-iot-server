package com.iviet.ivshs.dto.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.shared.enumeration.ClientType;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails extends User {
    private final Long id;
    private final ClientType clientType;
    private final String avatarUrl;
    private final Date lastLoginAt;
    private final List<String> groups;
    
    public CustomUserDetails(Client client, Collection<? extends GrantedAuthority> authorities) {
        super(client.getUsername(), client.getPasswordHash() != null ? client.getPasswordHash() : "", authorities);
        this.id = client.getId();
        this.clientType = client.getClientType();
        this.avatarUrl = client.getAvatarUrl();
        this.lastLoginAt = client.getLastLoginAt();
        this.groups = client.getGroups() != null ? client.getGroups().stream()
                .map(g -> g.getGroupCode())
                .collect(Collectors.toList()) : List.of();
    }
}