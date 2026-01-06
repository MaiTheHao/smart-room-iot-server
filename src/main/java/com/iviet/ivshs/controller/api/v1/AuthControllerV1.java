package com.iviet.ivshs.controller.api.v1;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.CreateClientDto;
import com.iviet.ivshs.dto.JwtResponse;
import com.iviet.ivshs.dto.LoginDto;
import com.iviet.ivshs.jwt.JwtUtils;
import com.iviet.ivshs.service.ClientServiceV1;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private ClientServiceV1 clientService;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<JwtResponse>> signin(
            @RequestBody @Valid LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), 
                loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Client client = clientService.getEntityByUsername(loginDto.getUsername());
        List<String> groupCodes = client.getGroups().stream()
            .map(g -> g.getGroupCode())
            .collect(Collectors.toList());
        
        JwtResponse jwtResponse = JwtResponse.of(
            jwt,
            userDetails.getUsername(),
            groupCodes
        );

        return ResponseEntity.ok(ApiResponse.ok(jwtResponse));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<ClientDto>> signup(
            @RequestBody @Valid CreateClientDto createDto) {
        ClientDto createdClient = clientService.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(createdClient));
    }
}

