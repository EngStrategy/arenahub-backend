package com.engstrategy.alugai_api.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados de login do usuário")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @Schema(description = "Email do usuário",
            example = "usuario@exemplo.com",
            required = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @Schema(description = "Senha do usuário",
            example = "12345678",
            required = true,
            minLength = 8)
    @NotBlank(message = "Senha é obrigatória")
    private String password;
}
