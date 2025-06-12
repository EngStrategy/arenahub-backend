package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.usuario.AuthResponse;
import com.engstrategy.alugai_api.dto.usuario.LoginRequest;
import com.engstrategy.alugai_api.exceptions.InvalidCredentialsException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
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
        Usuario usuario = userService.findUserByEmail(loginRequest.getEmail());

        if (usuario == null) {
            throw new UserNotFoundException("Usuário não encontrado");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getSenha())) {
            throw new InvalidCredentialsException("Senha inválida");
        }

        String token = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .accessToken(token)
                .userId(usuario.getId())
                .name(usuario.getNome())
                .role(usuario.getRole().toString())
                .expiresIn(jwtService.getExpirationInSeconds())
                .imageUrl(usuario.getUrlFoto())
                .build();
    }
}
