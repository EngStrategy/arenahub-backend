package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.usuario.AuthResponse;
import com.engstrategy.alugai_api.dto.usuario.LoginRequest;
import com.engstrategy.alugai_api.exceptions.EmailUnconfirmedException;
import com.engstrategy.alugai_api.exceptions.InvalidCredentialsException;
import com.engstrategy.alugai_api.jwt.JwtService;
import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse authenticate(LoginRequest loginRequest) {
        try {
            // Delega a autenticação para o Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Email ou senha inválidos");
        }

        // Se a autenticação foi bem-sucedida, busca o usuário
        Usuario usuario = userService.findUserByEmail(loginRequest.getEmail());

        if (!usuario.isAtivo()) {
            throw new EmailUnconfirmedException("Email não confirmado. Por favor, verifique sua caixa de entrada.");
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