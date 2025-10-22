package com.engstrategy.alugai_api.dto.atleta;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtivacaoRequestDTO {

    @NotBlank(message = "O telefone é obrigatório.")
    private String telefone;

}