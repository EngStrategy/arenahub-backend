package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.avaliacao.AvaliacaoDTO;
import com.engstrategy.alugai_api.dto.avaliacao.AvaliacaoResponseDTO;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.model.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoService {
    Optional<AvaliacaoResponseDTO> criarOuDispensarAvaliacao(Long agendamentoId, AvaliacaoDTO avaliacaoCreateDTO, CustomUserDetails userDetails);

    AvaliacaoResponseDTO atualizarAvaliacao(Long avaliacaoId, AvaliacaoDTO avaliacaoDTO, CustomUserDetails userDetails);

    Page<AvaliacaoResponseDTO> buscarAvaliacoesPorQuadra(Pageable pageable, Long quadraId);
}