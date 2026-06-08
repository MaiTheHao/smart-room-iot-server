package com.iviet.ivshs.controller.api.v1;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.service.client.ClientService;
import com.iviet.ivshs.dto.client.ClientDto;
import com.iviet.ivshs.dto.client.CreateClientDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.auth.CustomUserDetails;
import com.iviet.ivshs.dto.auth.JwtResponse;
import com.iviet.ivshs.dto.auth.LoginDto;
import com.iviet.ivshs.shared.security.JwtUtils;
import com.iviet.ivshs.shared.util.SecurityContextUtil;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;

        private final JwtUtils jwtUtils;

        private final ClientService clientService;

        @PostMapping("/signin")
        public ResponseEntity<ApiResponse<JwtResponse>> signin(@RequestBody
        @Valid
        LoginDto loginDto) {
                Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

                SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);

                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

                JwtResponse jwtResponse = JwtResponse.of(jwt, userDetails);

                return ResponseEntity.ok(ApiResponse.ok(jwtResponse));
        }

        @PostMapping("/signup")
        public ResponseEntity<ApiResponse<ClientDto>> signup(@RequestBody
        @Valid
        CreateClientDto createDto) {
                ClientDto createdClient = clientService.create(createDto);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.created(createdClient));
        }

        @GetMapping("/me")
        public ResponseEntity<ApiResponse<ClientDto>> getMe() {
                Long clientId = SecurityContextUtil.getCurrentClientId();
                ClientDto clientDto = clientService.getById(clientId);
                return ResponseEntity.ok(ApiResponse.ok(clientDto));
        }
}
