package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.SlotHorario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoService {

    Agendamento criarAgendamento(AgendamentoCreateDTO dto, Long atletaId);

    void cancelarAgendamento(Long agendamentoId, Long atletaId);

    Page<Agendamento> buscarPorAtletaId(Long atletaId, Pageable pageable);

    List<Agendamento> buscarAgendamentosPublicos(LocalDate dataInicio);

    Agendamento buscarPorId(Long agendamentoId);

    List<Agendamento> buscarPorQuadraEData(Long quadraId, LocalDate data);

    boolean verificarDisponibilidadeHorario(Long quadraId, LocalDate data,
                                            LocalTime inicio, LocalTime fim);
}
