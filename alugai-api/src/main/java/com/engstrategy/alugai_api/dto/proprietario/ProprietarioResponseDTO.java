package com.engstrategy.alugai_api.dto.proprietario;

import java.util.UUID;

public record ProprietarioResponseDTO(
        UUID id,
        String nome,
        String email,
        String telefone,
        String cpf
) {}

