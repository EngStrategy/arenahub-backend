package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoExternoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.PixPagamentoResponseDTO;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AgendamentoService {

    Agendamento criarAgendamento(AgendamentoCreateDTO dto, UUID atletaId);

    void cancelarAgendamento(Long agendamentoId, UUID atletaId);

    Page<Agendamento> buscarPorAtletaId(UUID atletaId,
                                        LocalDate dataInicio,
                                        LocalDate dataFim,
                                        TipoAgendamento tipoAgendamento,
                                        StatusAgendamento status,
                                        Pageable pageable);

    Agendamento buscarPorId(Long agendamentoId);

    Page<Agendamento> buscarPorArenaId(UUID arenaId,
                                       LocalDate dataInicio,
                                       LocalDate dataFim,
                                       StatusAgendamento status,
                                       Long quadraId,
                                       Pageable pageable);

    Agendamento atualizarStatus(Long agendamentoId, UUID arenaId, StatusAgendamento novoStatus);

    List<Agendamento> buscarAgendamentosParaAvaliacao(UUID atletaId);

    List<Agendamento> buscarPendentesAcaoPorArenaId(UUID arenaId);

    Agendamento criarAgendamentoExterno(AgendamentoExternoCreateDTO dto, UUID arenaId);

//    PixPagamentoResponseDTO criarPagamentoPix(AgendamentoCreateDTO dto, UUID atletaId);

    StatusAgendamento verificarStatus(Long agendamentoId);

    Page<Agendamento> buscarCardsMestrePorArenaId(
            UUID arenaId,
            LocalDate dataInicio,
            LocalDate dataFim,
            Long quadraId,
            StatusAgendamento status,
            Pageable pageable
    );

    List<Agendamento> buscarAgendamentosFixosFilhos(Long agendamentoFixoId, UUID arenaId);

    Page<Agendamento> buscarCardsMestrePorAtletaId(UUID atletaId,
                                                   LocalDate dataInicio,
                                                   LocalDate dataFim,
                                                   TipoAgendamento tipoAgendamento,
                                                   StatusAgendamento status,
                                                   Pageable pageable);

    List<Agendamento> buscarAgendamentosFixosFilhosAtleta(Long agendamentoFixoId, UUID atletaId);
}