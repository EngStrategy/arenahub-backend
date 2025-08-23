package com.engstrategy.alugai_api.dto.atleta;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AtletaBuscaResponseDTO {
    private Long id;
    private String nome;
    private String telefone;
    private String urlFoto;
}