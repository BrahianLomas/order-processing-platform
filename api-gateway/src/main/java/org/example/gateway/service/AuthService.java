package org.example.gateway.service;


import org.example.gateway.dto.LoginRequest;
import org.example.gateway.dto.LoginResponse;
import org.example.gateway.dto.RefreshResponse;
import org.example.gateway.dto.RegisterRequest;
import org.example.gateway.dto.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    RefreshResponse refresh(String token);
}
