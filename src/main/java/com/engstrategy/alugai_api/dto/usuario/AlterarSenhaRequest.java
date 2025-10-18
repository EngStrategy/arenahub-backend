package com.engstrategy.alugai_api.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Dados para alteração de senha do usuário logado")
public class AlterarSenhaRequest {

    @Schema(description = "Senha atual do usuário", required = true)
    @NotBlank(message = "A senha atual é obrigatória")
    private String senhaAtual;

    @Schema(description = "Nova senha do usuário", required = true, minLength = 8)
    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres")
    private String novaSenha;

    @Schema(description = "Confirmação da nova senha", required = true)
    @NotBlank(message = "A confirmação da nova senha é obrigatória")
    private String confirmacaoNovaSenha;

}
