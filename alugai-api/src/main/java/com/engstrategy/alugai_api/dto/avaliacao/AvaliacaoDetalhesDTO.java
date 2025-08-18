package com.engstrategy.alugai_api.dto.avaliacao;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvaliacaoDetalhesDTO {
    private Long idAvaliacao;
    private int nota;
    private String comentario;
}
