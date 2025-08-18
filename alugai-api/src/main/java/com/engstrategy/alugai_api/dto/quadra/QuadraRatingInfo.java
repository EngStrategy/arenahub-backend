package com.engstrategy.alugai_api.dto.quadra;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuadraRatingInfo {
    private Long quadraId;
    private Double notaMedia;
    private Long quantidadeAvaliacoes;
}