package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.AgendamentoFixo;

import java.util.List;

public interface AgendamentoFixoService {

    AgendamentoFixo criarAgendamentosFixos(Agendamento agendamentoBase);

    void cancelarAgendamentoFixo(Long agendamentoFixoId, Long usuarioId);

    List<AgendamentoFixo> listarAgendamentosFixosAtivos(Long atletaId);

    AgendamentoFixo buscarPorId(Long id);
}
