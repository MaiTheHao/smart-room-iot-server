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
import org.springframework.util.StringUtils;

import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.SysFunctionDao;
import com.iviet.ivshs.dto.CustomUserDetails;
import com.iviet.ivshs.entities.Client;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final ClientDao clientDao;
  private final SysFunctionDao functionDao;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if (!StringUtils.hasText(username)) {
      log.warn("Authentication failed: Attempted login with empty username");
      throw new UsernameNotFoundException("Username cannot be null or empty");
    }

    String cleanUsername = username.trim();
    log.trace("Searching for user: {}", cleanUsername);

    Client client = clientDao.findByUsername(cleanUsername)
        .orElseGet(() -> {
          log.warn("Authentication failed: User [{}] not found", cleanUsername);
          throw new UsernameNotFoundException("User not found: " + cleanUsername);
        });

    return buildUserDetails(client);
  }

  private CustomUserDetails buildUserDetails(Client client) {
    List<String> functionCodes = functionDao.findAllByClientId(client.getId(), "null")
        .stream()
        .map(func -> func.functionCode())
        .collect(Collectors.toList());

    Set<SimpleGrantedAuthority> authorities = functionCodes.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());

    log.info("User authenticated: ID={}, Username={}, AuthoritiesCount={}", 
        client.getId(), client.getUsername(), authorities.size());
    log.debug("Granting authorities to [{}]: {}", client.getUsername(), functionCodes);

    return new CustomUserDetails(client, authorities);
  }
}