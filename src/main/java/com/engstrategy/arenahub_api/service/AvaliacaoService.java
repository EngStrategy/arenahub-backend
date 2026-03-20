package com.engstrategy.arenahub_api.service;

import com.engstrategy.arenahub_api.dto.avaliacao.AvaliacaoDTO;
import com.engstrategy.arenahub_api.dto.avaliacao.AvaliacaoResponseDTO;
import com.engstrategy.arenahub_api.jwt.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AvaliacaoService {
    Optional<AvaliacaoResponseDTO> criarOuDispensarAvaliacao(Long agendamentoId, AvaliacaoDTO avaliacaoCreateDTO, CustomUserDetails userDetails);

    AvaliacaoResponseDTO atualizarAvaliacao(Long avaliacaoId, AvaliacaoDTO avaliacaoDTO, CustomUserDetails userDetails);

    Page<AvaliacaoResponseDTO> buscarAvaliacoesPorQuadra(Pageable pageable, Long quadraId);
}