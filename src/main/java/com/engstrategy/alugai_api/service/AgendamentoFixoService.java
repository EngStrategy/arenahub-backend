package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.AgendamentoFixo;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AgendamentoFixoService {

    AgendamentoFixo criarAgendamentosFixos(Agendamento agendamentoBase);

    void cancelarAgendamentoFixo(Long agendamentoFixoId, UUID usuarioId);

    AgendamentoFixo buscarPorId(Long id);

    List<LocalDate> preValidarAgendamentoFixo(Agendamento agendamentoBase);

    void cancelarAgendamentoFixoPorArena(Long agendamentoFixoId, UUID arenaId);

    LocalDate buscarProximaDataRecorrente(LocalDate dataReferencia, DiaDaSemana diaDaSemana);

    AgendamentoFixo criarInstanciasDeAula(AgendamentoFixo aulaFixo, Agendamento agendamentoBase);
}
