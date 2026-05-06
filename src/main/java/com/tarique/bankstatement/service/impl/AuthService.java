package com.tarique.bankstatement.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tarique.bankstatement.dto.request.LoginRequest;
import com.tarique.bankstatement.dto.response.JwtResponse;
import com.tarique.bankstatement.repository.UserRepository;
import com.tarique.bankstatement.security.jwt.JwtProvider;
import com.tarique.bankstatement.security.service.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthService — handles login and logout logic.
 * Stores JWT in DB so server-side invalidation (logout) is possible.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider           jwtProvider;
    private final UserRepository        userRepository;
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Transactional
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String jwt = jwtProvider.generateToken(authentication);

        // Persist token so we can invalidate it on logout
        userRepository.findByUsername(principal.getUsername()).ifPresent(user -> {
            user.setAccessToken(jwt);
            userRepository.save(user);
        });

        log.info("User '{}' logged in successfully", principal.getUsername());

        return JwtResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .username(principal.getUsername())
                .email(principal.getEmail())
                .role(principal.getRole())
                .build();
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAccessToken(null);
            userRepository.save(user);
            log.info("User '{}' logged out — token invalidated", username);
        });
    }
}
