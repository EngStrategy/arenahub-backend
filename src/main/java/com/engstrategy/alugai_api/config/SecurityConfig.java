package com.engstrategy.alugai_api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final AuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        // endpoints públicos
                        .requestMatchers("/api/v1/usuarios/auth").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/atletas").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/atletas/iniciar-ativacao").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/atletas/ativar-conta").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/arenas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/atletas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/arenas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/arenas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/quadras").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/quadras/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/esportes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/jogos-abertos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/arenas/cidades").permitAll()
                        .requestMatchers("/api/v1/verify").permitAll()
                        .requestMatchers("/api/v1/resend-verification").permitAll()
                        .requestMatchers("/api/v1/forgot-password").permitAll()
                        .requestMatchers("/api/v1/verify-reset-code").permitAll()
                        .requestMatchers("/api/v1/reset-password").permitAll()
                        .requestMatchers("/api/v1/feedback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/stripe/webhook").permitAll()
                        // endpoints com autorização
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/agendamentos/**").hasAnyRole("ATLETA", "ARENA")
                        .requestMatchers("/api/v1/jogos-abertos/**").hasRole("ATLETA")
                        .requestMatchers("/api/v1/agendamentos/**").hasRole("ATLETA")
                        .requestMatchers("/api/v1/atletas/buscar-atleta").hasRole("ARENA")
                        .requestMatchers("/api/v1/atletas/**").hasRole("ATLETA")
                        .requestMatchers("/api/v1/arenas/**").hasRole("ARENA")
                        .requestMatchers("/api/v1/quadras/**").hasRole("ARENA")
                        .requestMatchers("/api/v1/arena/agendamentos").hasRole("ARENA")
                        .requestMatchers("/api/v1/arena/agendamentos/**").hasRole("ARENA")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/atletas/me/alterar-senha").hasRole("ATLETA")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/arenas/me/alterar-senha").hasRole("ARENA")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origens permitidas para desenvolvimento e produção
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://arenahub.app"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers específicos que sua aplicação usa
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Permite credenciais
         configuration.setAllowCredentials(true);

        // Headers expostos para o frontend
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
