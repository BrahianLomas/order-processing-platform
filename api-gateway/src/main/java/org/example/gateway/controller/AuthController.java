package org.example.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.example.gateway.dto.LoginRequest;
import org.example.gateway.dto.LoginResponse;
import org.example.gateway.dto.RefreshResponse;
import org.example.gateway.dto.RegisterRequest;
import org.example.gateway.dto.RegisterResponse;
import org.example.gateway.service.AuthService;
import org.example.shared.response.GenericResponse;
import org.example.shared.response.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<GenericResponse> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);
            response.markSuccess();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GenericResponse.error(ResponseCode.BUSINESS_ERROR, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            response.markSuccess();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GenericResponse.error(ResponseCode.UNAUTHORIZED, e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<GenericResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            RefreshResponse response = authService.refresh(token);
            response.markSuccess();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GenericResponse.error(ResponseCode.UNAUTHORIZED, e.getMessage()));
        }
    }
}
