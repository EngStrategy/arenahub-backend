package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.jogosabertos.JogoAbertoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.MinhaParticipacaoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.SolicitacaoEntradaDTO;

import java.util.List;

public interface JogoAbertoService {

    List<JogoAbertoResponseDTO> listarJogosAbertos();

    SolicitacaoEntradaDTO solicitarEntrada(Long agendamentoId, Long atletaId);

    List<SolicitacaoEntradaDTO> listarSolicitacoes(Long agendamentoId, Long proprietarioId);

    SolicitacaoEntradaDTO gerenciarSolicitacao(Long solicitacaoId, Long proprietarioId, boolean aceitar);

    void sairDeJogoAberto(Long solicitacaoId, Long atletaId);

    List<MinhaParticipacaoResponseDTO> listarMinhasParticipacoes(Long atletaId);

}
