package com.engstrategy.alugai_api.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Resposta de autenticação com token JWT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    @Schema(description = "Token JWT para autenticação",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQGV4ZW1wbG8uY29tIiwiaWF0IjoxNjMwNTM5NjAwLCJleHAiOjE2MzA1NDMyMDB9.signature")
    private String accessToken;

    @Schema(description = "ID único do usuário",
            example = "1")
    private Long userId;

    @Schema(description = "Nome do usuário",
            example = "João Silva")
    private String nome;

    @Schema(description = "Tipo de usuário",
            example = "ATLETA",
            allowableValues = {"ATLETA", "ARENA"})
    private String role;

    @Schema(description = "Tempo de expiração do token em segundos",
            example = "3600")
    private long expiresIn;
}
