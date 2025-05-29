package com.engstrategy.alugai_api.dto.proprietario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record ProprietarioCadastroDTO(
        @NotBlank String nome,
        @Email @NotBlank String email,
        @NotBlank String telefone,
        @NotBlank String senha,
        @NotBlank @CPF String cpf
) {}

