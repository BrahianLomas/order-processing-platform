package org.example.gateway.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.gateway.dto.LoginRequest;
import org.example.gateway.dto.LoginResponse;
import org.example.gateway.dto.RefreshResponse;
import org.example.gateway.dto.RegisterRequest;
import org.example.gateway.dto.RegisterResponse;
import org.example.gateway.dto.UserSummary;
import org.example.gateway.entity.User;
import org.example.gateway.repository.UserRepository;
import org.example.gateway.service.AuthService;
import org.example.shared.exception.BusinessException;
import org.example.shared.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email ya registrado");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole("USER");
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            throw new BusinessException("INVALID_CREDENTIALS", "Email o contraseña inválido");
        }

        User user = userOpt.get();

        if (!user.getActive()) {
            throw new BusinessException("USER_INACTIVE", "Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Email o contraseña inválido");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        UserSummary userSummary = new UserSummary(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());

        return new LoginResponse(token, "Bearer", jwtExpiration, userSummary);
    }

    @Override
    public RefreshResponse refresh(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException("INVALID_TOKEN", "Token inválido o expirado");
        }

        Long userId = jwtUtil.extractUserId(token);

        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new BusinessException("USER_NOT_FOUND", "Usuario no encontrado");
        }

        User user = userOpt.get();

        String newToken = jwtUtil.generateToken(user.getId(), user.getEmail());

        return new RefreshResponse(newToken, "Bearer", jwtExpiration);
    }
}
