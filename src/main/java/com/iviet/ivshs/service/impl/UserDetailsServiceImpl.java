package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Set;

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
import com.iviet.ivshs.enumeration.ClientType;
import com.iviet.ivshs.exception.domain.InvalidClientTypeException;

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
            logger.warn("Attempted to load user with null or empty username");
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }

        Client client = clientDao.findUserByUsername(username).orElseThrow(() -> {
            logger.warn("Authentication attempt for non-existent user: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        });

        if (client.getClientType() != ClientType.USER) throw new InvalidClientTypeException("Client type is not USER");

        return buildUserDetails(username, client);
    }

    private CustomUserDetails buildUserDetails(String username, Client client) {
        List<String> functionCodes = clientFunctionCacheDao.getFunctionCodesByClient(client.getId());
        logger.info("Loaded user: {} with {} permissions", username, functionCodes.size());

        Set<SimpleGrantedAuthority> authorities = functionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(java.util.stream.Collectors.toSet());

        return new CustomUserDetails(client, authorities);
    }
}
