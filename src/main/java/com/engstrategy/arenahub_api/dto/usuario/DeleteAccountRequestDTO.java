package com.engstrategy.arenahub_api.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados para exclusão de conta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAccountRequestDTO {

    @Schema(description = "Senha do usuário para confirmação",
            example = "12345678",
            required = true)
    @NotBlank(message = "Senha é obrigatória")
    private String password;
}
