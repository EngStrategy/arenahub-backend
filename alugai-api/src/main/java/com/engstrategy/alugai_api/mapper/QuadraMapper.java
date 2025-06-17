package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.quadra.QuadraCreateDTO;
import com.engstrategy.alugai_api.dto.quadra.QuadraResponseDTO;
import com.engstrategy.alugai_api.model.Quadra;

import org.springframework.stereotype.Component;

@Component
public class QuadraMapper {

    public Quadra mapQuadraCreateDtoToQuadra(QuadraCreateDTO quadraCreateDTO) {
        return Quadra.builder()
                .nomeQuadra(quadraCreateDTO.getNomeQuadra())
                .urlFotoQuadra(quadraCreateDTO.getUrlFotoQuadra())
                .tipoQuadra(quadraCreateDTO.getTipoQuadra())
                .descricao(quadraCreateDTO.getDescricao())
                .duracaoReserva(quadraCreateDTO.getDuracaoReserva())
                .cobertura(quadraCreateDTO.isCobertura())
                .iluminacaoNoturna(quadraCreateDTO.isIluminacaoNoturna())
                .materiaisFornecidos(quadraCreateDTO.getMateriaisFornecidos())
                .build();
    }

    public QuadraResponseDTO mapQuadraToQuadraResponseDTO(Quadra quadra) {
        return QuadraResponseDTO.builder()
                .nomeQuadra(quadra.getNomeQuadra())
                .urlFotoQuadra(quadra.getUrlFotoQuadra())
                .tipoQuadra(quadra.getTipoQuadra())
                .descricao(quadra.getDescricao())
                .duracaoReserva(quadra.getDuracaoReserva())
                .cobertura(quadra.isCobertura())
                .iluminacaoNoturna(quadra.isIluminacaoNoturna())
                .materiaisFornecidos(quadra.getMateriaisFornecidos())
                .arenaId(quadra.getArena().getId())
                .nomeArena(quadra.getArena().getNome())
                .build();
    }
}
