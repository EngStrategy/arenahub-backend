package com.engstrategy.alugai_api.dto.atleta;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AtletaBuscaResponseDTO {
    private UUID id;
    private String nome;
    private String telefone;
    private String urlFoto;
}