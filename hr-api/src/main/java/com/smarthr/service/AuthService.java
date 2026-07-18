package com.smarthr.service;

import com.smarthr.dto.JwtResponse;
import com.smarthr.dto.LoginRequest;
import com.smarthr.dto.RefreshTokenRequest;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    JwtResponse refreshAccessToken(RefreshTokenRequest refreshTokenRequest);
}
