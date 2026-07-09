package com.iviet.ivshs.service.auth.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.iviet.ivshs.dto.GroupPermissionMapping;
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

    Client client = clientDao.findByUsername(cleanUsername).orElseGet(() -> {
      log.warn("Authentication failed: User [{}] not found", cleanUsername);
      throw new UsernameNotFoundException("User not found: " + cleanUsername);
    });

    List<GroupPermissionMapping> mappings = functionDao.findGroupPermissionMappingsByClientId(client.getId());

    Map<Long, Set<String>> groupPermissions = new HashMap<>();
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();

    for (GroupPermissionMapping mapping : mappings) {
      authorities.add(new SimpleGrantedAuthority(mapping.functionCode()));
      groupPermissions
          .computeIfAbsent(mapping.groupId(), k -> new HashSet<>())
          .add(mapping.functionCode());
    }

    log.info("User authenticated: ID={}, Username={}, AuthoritiesCount={}, GroupPermissionsMappingCount={}",
        client.getId(), client.getUsername(), authorities.size(), groupPermissions.size());
    return new CustomUserDetails(client, authorities, groupPermissions);
  }
}
