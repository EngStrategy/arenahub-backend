package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoService {

    Agendamento criarAgendamento(AgendamentoCreateDTO dto, Long atletaId);

    void cancelarAgendamento(Long agendamentoId, Long atletaId);

    Page<Agendamento> buscarPorAtletaId(Long atletaId,
                                        LocalDate dataInicio,
                                        LocalDate dataFim,
                                        TipoAgendamento tipoAgendamento,
                                        StatusAgendamento status,
                                        Pageable pageable);

    Agendamento buscarPorId(Long agendamentoId);

    Page<Agendamento> buscarPorArenaId(Long arenaId,
                                       LocalDate dataInicio,
                                       LocalDate dataFim,
                                       StatusAgendamento status,
                                       Long quadraId,
                                       Pageable pageable);

    Agendamento atualizarStatus(Long agendamentoId, Long arenaId, StatusAgendamento novoStatus);
}
