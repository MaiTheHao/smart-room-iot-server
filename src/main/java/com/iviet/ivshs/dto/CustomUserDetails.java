package com.iviet.ivshs.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.shared.enumeration.ClientType;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails extends User {
    private final Long id;
    private final ClientType clientType;
    private final String avatarUrl;

    @Getter(lombok.AccessLevel.NONE)
    private final Date lastLoginAt;

    private final Set<String> groups;

    private final Map<Long, Set<String>> groupPermissions;

    public CustomUserDetails(Client client, Collection<? extends GrantedAuthority> authorities, Map<Long, Set<String>> groupPermissions) {
        super(Objects.requireNonNull(client, "Client must not be null").getUsername(),
                Objects.requireNonNullElse(client.getPasswordHash(), ""), authorities);

        this.id = client.getId();
        this.clientType = client.getClientType();
        this.avatarUrl = client.getAvatarUrl();

        this.lastLoginAt = client.getLastLoginAt() != null ? new Date(client.getLastLoginAt().getTime()) : null;

        this.groups = client.getGroups() != null
                ? client.getGroups().stream().map(g -> g.getGroupCode()).collect(Collectors.toUnmodifiableSet())
                : Set.of();

        if (groupPermissions == null) {
            this.groupPermissions = Map.of();
        } else {
            Map<Long, Set<String>> tempMap = new HashMap<>();
            groupPermissions.forEach((groupId, permissions) -> {
                tempMap.put(groupId, Set.copyOf(permissions));
            });
            this.groupPermissions = Map.copyOf(tempMap);
        }
    }

    public Date getLastLoginAt() {
        return this.lastLoginAt != null ? new Date(this.lastLoginAt.getTime()) : null;
    }

    public boolean hasPermission(Long groupId, String permission) {
        if (groupPermissions == null || groupId == null || permission == null) {
            return false;
        }
        Set<String> permissions = groupPermissions.get(groupId);
        return permissions != null && permissions.contains(permission);
    }

    public Set<Long> getAllowedGroups(String permission) {
        if (groupPermissions == null || permission == null) {
            return Set.of();
        }
        return groupPermissions.entrySet().stream()
                .filter(entry -> entry.getValue().contains(permission))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }
}
