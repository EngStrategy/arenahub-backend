package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.jogosabertos.JogoAbertoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.MinhaParticipacaoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.SolicitacaoEntradaDTO;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.SolicitacaoEntrada;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class JogoAbertoMapper {

    public JogoAbertoResponseDTO toJogoAbertoResponseDTO(Agendamento agendamento) {
        if (agendamento == null) {
            return null;
        }

        return JogoAbertoResponseDTO.builder()
                .agendamentoId(agendamento.getId())
                .data(agendamento.getDataAgendamento())
                .horarioInicio(agendamento.getHorarioInicio())
                .horarioFim(agendamento.getHorarioFim())
                .vagasDisponiveis(agendamento.getVagasDisponiveis())
                .esporte(agendamento.getEsporte())
                .nomeArena(agendamento.getQuadra().getArena().getNome())
                .nomeQuadra(agendamento.getQuadra().getNomeQuadra())
                .cidade(agendamento.getQuadra().getArena().getEndereco().getCidade())
                .urlFotoQuadra(agendamento.getQuadra().getUrlFotoQuadra())
                .build();
    }

    public SolicitacaoEntradaDTO toSolicitacaoEntradaDTO(SolicitacaoEntrada solicitacao) {
        if (solicitacao == null) {
            return null;
        }

        return SolicitacaoEntradaDTO.builder()
                .id(solicitacao.getId())
                .agendamentoId(solicitacao.getAgendamento().getId())
                .solicitanteId(solicitacao.getSolicitante().getId())
                .nomeSolicitante(solicitacao.getSolicitante().getNome())
                .fotoSolicitante(solicitacao.getSolicitante().getUrlFoto())
                .status(solicitacao.getStatus())
                .build();
    }

    public MinhaParticipacaoResponseDTO toMinhaParticipacaoResponseDTO(SolicitacaoEntrada solicitacao) {
        if (solicitacao == null || solicitacao.getAgendamento() == null) {
            return null;
        }

        Agendamento agendamento = solicitacao.getAgendamento();

        return MinhaParticipacaoResponseDTO.builder()
                .solicitacaoId(solicitacao.getId())
                .agendamentoId(agendamento.getId())
                .nomeArena(agendamento.getQuadra().getArena().getNome())
                .nomeQuadra(agendamento.getQuadra().getNomeQuadra())
                .urlFotoQuadra(agendamento.getQuadra().getUrlFotoQuadra())
                .data(agendamento.getDataAgendamento())
                .horarioInicio(agendamento.getHorarioInicio())
                .horarioFim(agendamento.getHorarioFim())
                .esporte(agendamento.getEsporte())
                .status(solicitacao.getStatus())
                .build();
    }
}