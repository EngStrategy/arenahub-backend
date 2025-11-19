package com.engstrategy.alugai_api.config;

import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.jwt.JwtService;
import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.model.enums.Role;
import com.engstrategy.alugai_api.service.impl.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserServiceImpl userServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Se não tiver header ou não for Bearer, segue o fluxo (sem autenticar)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            // Extrai dados do token
            Role role = jwtService.getRoleFromToken(token);
            UUID userId = jwtService.getUserIdFromToken(token);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Busca o usuário no banco para garantir que ainda existe e está ativo
                Usuario usuario = userServiceImpl.findUserById(userId, role);

                if (usuario != null) {
                    CustomUserDetails userDetails = new CustomUserDetails(
                            usuario.getEmail(),
                            usuario.getId(),
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name())),
                            usuario.isAtivo(),
                            usuario.getSenha(),
                            usuario.getRole()
                    );

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // Autentica no contexto
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // AQUI ESTÁ A CORREÇÃO:
            // Não lançamos exceção. Apenas logamos que o token é inválido.
            // Se o usuário estiver tentando acessar uma rota protegida, o Spring Security vai barrar
            // porque o SecurityContextHolder estará vazio.
            // Se ele estiver tentando Logar (rota pública), vai passar e funcionar.
            log.warn("Token inválido ou expirado recebido na requisição: {}", e.getMessage());
        }

        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}

