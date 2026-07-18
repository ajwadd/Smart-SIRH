package com.smarthr.service.impl;

import com.smarthr.dto.JwtResponse;
import com.smarthr.dto.LoginRequest;
import com.smarthr.dto.RefreshTokenRequest;
import com.smarthr.entity.User;
import com.smarthr.exception.ErrorConstants;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.repository.UserRepository;
import com.smarthr.security.JwtTokenProvider;
import com.smarthr.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND, 
                        "Utilisateur non trouvé avec le nom d'utilisateur: " + userDetails.getUsername()));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return JwtResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    public JwtResponse refreshAccessToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (tokenProvider.validateToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            String newAccessToken = tokenProvider.generateAccessToken(userDetails);
            String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND, 
                            "Utilisateur non trouvé avec le nom d'utilisateur: " + username));

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return JwtResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles)
                    .build();
        } else {
            throw new IllegalArgumentException("Token de rafraîchissement invalide ou expiré");
        }
    }
}
