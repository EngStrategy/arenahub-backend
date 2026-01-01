package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.jogosabertos.JogoAbertoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.MinhaParticipacaoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.SolicitacaoEntradaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface JogoAbertoService {

    Page<JogoAbertoResponseDTO> listarJogosAbertos(
        Pageable pageable,
        String cidade,
        String esporte,
        Double latitude,
        Double longitude,
        Double raioKm,
        UUID atletaLogadoId
    );

    SolicitacaoEntradaDTO solicitarEntrada(Long agendamentoId, UUID atletaId);

    List<SolicitacaoEntradaDTO> listarSolicitacoes(Long agendamentoId, UUID proprietarioId);

    SolicitacaoEntradaDTO gerenciarSolicitacao(Long solicitacaoId, UUID proprietarioId, boolean aceitar);

    void sairDeJogoAberto(Long solicitacaoId, UUID atletaId);

    List<MinhaParticipacaoResponseDTO> listarMinhasParticipacoes(UUID atletaId);

}
