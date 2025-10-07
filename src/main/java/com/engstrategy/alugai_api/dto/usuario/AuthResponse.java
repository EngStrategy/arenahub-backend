package com.engstrategy.alugai_api.dto.usuario;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "Resposta de autenticação com token JWT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    @Schema(description = "Token JWT para autenticação",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQGV4ZW1wbG8uY29tIiwiaWF0IjoxNjMwNTM5NjAwLCJleHAiOjE2MzA1NDMyMDB9.signature")
    private String accessToken;

    @Schema(description = "ID único do usuário",
            example = "1")
    private UUID userId;

    @Schema(description = "Nome do usuário",
            example = "João Silva")
    private String name;

    @Schema(description = "Tipo de usuário",
            example = "ATLETA",
            allowableValues = {"ATLETA", "ARENA"})
    private String role;

    @Schema(description = "Tempo de expiração do token em segundos",
            example = "86400")
    private long expiresIn;

    @Schema(description = "URL da foto do usuário", example = "https://imagem.com.br")
    private String imageUrl;

    @Schema(description = "Status da assinatura (apenas para Arenas)", example = "ATIVA")
    private String statusAssinatura;

    @Schema(description = "CPF ou CNPJ do usuário", example = "123.456.789-00 ou 12.345.678/0001-00")
    private String cpfCnpj;

}
