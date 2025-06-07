package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.usuario.AuthResponse;
import com.engstrategy.alugai_api.dto.usuario.LoginRequest;
import com.engstrategy.alugai_api.jwt.JwtService;
import com.engstrategy.alugai_api.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService; // Add UserService dependency
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse authenticate(LoginRequest loginRequest) {
        Usuario usuario = userService.findUserByEmail(loginRequest.getEmail()); // Use UserService

        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        if (!passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        String token = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .accessToken(token)
                .userId(usuario.getId())
                .nome(usuario.getNome())
                .role(usuario.getRole().toString())
                .expiresIn(3600) // 1 hora em segundos
                .build();
    }
}
