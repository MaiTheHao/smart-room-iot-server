package com.iviet.ivshs.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.enumeration.ClientTypeV1;
import com.iviet.ivshs.exception.domain.InvalidClientTypeException;

@Service
public class UserDetailsServiceImplV1 implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImplV1.class);
    private static final String DEFAULT_ROLE = "USER";

    @Autowired
    private ClientDao clientDao;

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

        if (client.getClientType() != ClientTypeV1.USER) throw new InvalidClientTypeException("Client type is not USER");

        return buildUserDetails(username, client);
    }

    private UserDetails buildUserDetails(String username, Client client) {
        return User.builder()
                .username(username)
                .password(client.getPasswordHash())
                .roles(DEFAULT_ROLE)
                .build();
    }
}
