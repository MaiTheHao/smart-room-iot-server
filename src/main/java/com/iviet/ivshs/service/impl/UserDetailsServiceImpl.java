package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.SysClientFunctionCacheDao;
import com.iviet.ivshs.dto.CustomUserDetails;
import com.iviet.ivshs.entities.Client;

@Slf4j(topic = "USER_DETAILS_SERVICE")
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ClientDao clientDao;
    private final SysClientFunctionCacheDao clientFunctionCacheDao;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cleanUsername = username != null ? username.trim() : null;
        if (cleanUsername == null || cleanUsername.isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }

        Client client = clientDao.findByUsername(cleanUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + cleanUsername));

        return buildUserDetails(client);
    }

    private CustomUserDetails buildUserDetails(Client client) {
        List<String> functionCodes = clientFunctionCacheDao.getFunctionCodesByClient(client.getId());

        Set<SimpleGrantedAuthority> authorities = functionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        log.info("Authenticated: {} [ID: {}] - Permissions: {}", client.getUsername(), client.getId(), authorities.size());

        return new CustomUserDetails(client, authorities);
    }
}
