package com.engstrategy.alugai_api.dto.avaliacao;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AvaliacaoResponseDTO {
    private Long id;
    private Integer nota;
    private String comentario;
    private LocalDateTime dataAvaliacao;
    private String nomeAtleta;
    private String urlFotoAtleta;

}
