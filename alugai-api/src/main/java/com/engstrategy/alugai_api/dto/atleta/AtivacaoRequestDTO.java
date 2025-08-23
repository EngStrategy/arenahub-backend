package com.engstrategy.alugai_api.dto.atleta;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para receber a solicitação de início de ativação de conta.
 * Requer apenas o número de telefone do atleta.
 */
@Getter
@Setter
public class AtivacaoRequestDTO {

    @NotBlank(message = "O telefone é obrigatório.")
    private String telefone;

}