package com.engstrategy.alugai_api.jwt;

import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecretKeyGenerator secretKeyGenerator;

    public String generateToken(Usuario user) {
        var key = secretKeyGenerator.getKey();
        var expirationDate = generateExpirationDate();
        var claims = generateTokenClaims(user);

        return Jwts
                .builder()
                .signWith(key)
                .subject(user.getEmail())
                .expiration(expirationDate)
                .claims(claims)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            var key = secretKeyGenerator.getKey();
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Token inválido");
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }

    public Role getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        String role = claims.get("role", String.class);
        return Role.valueOf(role);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("id", Long.class);
    }

    private Date generateExpirationDate() {
        var expirationMinutes = 60;
        LocalDateTime now = LocalDateTime.now().plusMinutes(expirationMinutes);
        return Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Map<String, Object> generateTokenClaims(Usuario user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", user.getNome());
        claims.put("id", user.getId());
        claims.put("role", user.getRole().toString());
        return claims;
    }
}