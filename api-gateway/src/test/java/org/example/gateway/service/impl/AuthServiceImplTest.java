package org.example.gateway.service.impl;

import org.example.gateway.dto.LoginRequest;
import org.example.gateway.dto.LoginResponse;
import org.example.gateway.dto.RegisterRequest;
import org.example.gateway.dto.RegisterResponse;
import org.example.gateway.entity.User;
import org.example.gateway.repository.UserRepository;
import org.example.shared.exception.BusinessException;
import org.example.shared.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtUtil = Mockito.mock(JwtUtil.class);
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtUtil);
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86_400_000L);
    }

    private User existingUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("demo@test.com");
        user.setPassword("hashed-password");
        user.setFirstName("Demo");
        user.setLastName("User");
        user.setRole("USER");
        user.setActive(true);
        return user;
    }

    @Test
    void register_newEmail_savesUserAndReturnsResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("secret123");
        request.setFirstName("New");
        request.setLastName("User");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        RegisterResponse response = authService.register(request);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getEmail()).isEqualTo("new@test.com");
        assertThat(response.getFirstName()).isEqualTo("New");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailAlreadyExists_throwsBusinessException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("demo@test.com");
        request.setPassword("secret123");
        request.setFirstName("Demo");
        request.setLastName("User");

        when(userRepository.findByEmail("demo@test.com")).thenReturn(Optional.of(existingUser()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void login_correctCredentials_returnsTokenAndUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("demo@test.com");
        request.setPassword("secret123");

        when(userRepository.findByEmail("demo@test.com")).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches("secret123", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "demo@test.com")).thenReturn("fake-jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86_400_000L);
        assertThat(response.getUser().getEmail()).isEqualTo("demo@test.com");
    }

    @Test
    void login_wrongPassword_throwsBusinessException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("demo@test.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("demo@test.com")).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("INVALID_CREDENTIALS"));
    }

    @Test
    void login_unknownEmail_throwsBusinessException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@test.com");
        request.setPassword("secret123");

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("INVALID_CREDENTIALS"));

        Mockito.verify(passwordEncoder, Mockito.never()).matches(anyString(), anyString());
    }

    @Test
    void login_inactiveUser_throwsBusinessException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("demo@test.com");
        request.setPassword("secret123");

        User inactiveUser = existingUser();
        inactiveUser.setActive(false);
        when(userRepository.findByEmail("demo@test.com")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("USER_INACTIVE"));
    }
}
