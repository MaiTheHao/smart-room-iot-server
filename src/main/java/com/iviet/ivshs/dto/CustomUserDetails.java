package com.iviet.ivshs.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.iviet.ivshs.entities.Client;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final Long id;
    
    public CustomUserDetails(Client client, Collection<? extends GrantedAuthority> authorities) {
        super(client.getUsername(), client.getPasswordHash(), authorities);
        this.id = client.getId();
    }
}