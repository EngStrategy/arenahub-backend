package com.engstrategy.alugai_api.dto.atleta;

import java.util.UUID;

public record AtletaResponseDTO(
        UUID id,
        String nome,
        String email,
        String telefone
) {}

