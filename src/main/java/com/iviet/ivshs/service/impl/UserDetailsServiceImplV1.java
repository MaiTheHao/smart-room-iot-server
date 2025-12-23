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

import com.iviet.ivshs.dao.ClientDaoV1;
import com.iviet.ivshs.entities.ClientV1;

@Service
public class UserDetailsServiceImplV1 implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImplV1.class);
    private static final String DEFAULT_ROLE = "USER";

    @Autowired
    private ClientDaoV1 clientDao;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cleanUsername = username != null ? username.trim() : null;
        if (cleanUsername == null || cleanUsername.isEmpty()) {
            logger.warn("Attempted to load user with null or empty username");
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }

        ClientV1 client = clientDao.findByUsername(username);
        
        if (client == null) {
            logger.warn("Authentication attempt for non-existent user: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return buildUserDetails(username, client);
    }

    private UserDetails buildUserDetails(String username, ClientV1 client) {
        return User.builder()
                .username(username)
                .password(client.getPasswordHash())
                .roles(DEFAULT_ROLE)
                .build();
    }
}
