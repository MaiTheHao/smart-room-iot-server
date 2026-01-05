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

import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.dto.ApiResponseV1;
import com.iviet.ivshs.dto.ClientDtoV1;
import com.iviet.ivshs.dto.CreateClientDtoV1;
import com.iviet.ivshs.dto.JwtResponseV1;
import com.iviet.ivshs.dto.LoginDtoV1;
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
    public ResponseEntity<ApiResponseV1<JwtResponseV1>> signin(
            @RequestBody @Valid LoginDtoV1 loginDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), 
                loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ClientV1 client = clientService.getEntityByUsername(loginDto.getUsername());
        List<String> groupCodes = client.getGroups().stream()
            .map(g -> g.getGroupCode())
            .collect(Collectors.toList());
        
        JwtResponseV1 jwtResponse = JwtResponseV1.of(
            jwt,
            userDetails.getUsername(),
            groupCodes
        );

        return ResponseEntity.ok(ApiResponseV1.ok(jwtResponse));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseV1<ClientDtoV1>> signup(
            @RequestBody @Valid CreateClientDtoV1 createDto) {
        ClientDtoV1 createdClient = clientService.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseV1.created(createdClient));
    }
}

