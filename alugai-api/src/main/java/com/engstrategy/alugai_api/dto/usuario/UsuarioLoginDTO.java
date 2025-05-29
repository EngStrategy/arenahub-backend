package com.engstrategy.alugai_api.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioLoginDTO(
        @Email @NotBlank String email,
        @NotBlank String senha
) {}

