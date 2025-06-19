package com.engstrategy.alugai_api.dto.atleta;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AtletaCadastroDTO(
        @NotBlank String nome,
        @Email @NotBlank String email,
        @NotBlank String telefone,
        @NotBlank String senha
) {}
