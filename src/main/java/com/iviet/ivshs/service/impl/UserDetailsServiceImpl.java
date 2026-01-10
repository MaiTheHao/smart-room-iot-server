package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SysClientFunctionCacheDao clientFunctionCacheDao;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cleanUsername = username != null ? username.trim() : null;
        if (cleanUsername == null || cleanUsername.isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }

        Client client = clientDao.findUserByUsername(cleanUsername).orElseThrow(() -> {
            logger.warn("User not found: {}", cleanUsername);
            return new UsernameNotFoundException("User not found: " + cleanUsername);
        });

        return buildUserDetails(client);
    }

    private CustomUserDetails buildUserDetails(Client client) {
        List<String> functionCodes = clientFunctionCacheDao.getFunctionCodesByClient(client.getId());
        
        Set<SimpleGrantedAuthority> authorities = functionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        logger.info("Authenticated: {} [ID: {}] - Permissions: {}", client.getUsername(), client.getId(), authorities.size());
        
        return new CustomUserDetails(client, authorities);
    }
}