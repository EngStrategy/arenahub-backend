package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.model.Agendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AgendamentoService {
    Agendamento criarAgendamento(AgendamentoCreateDTO agendamentoCreateDTO, Long atletaId);
    Page<Agendamento> buscarPorAtletaId(Long atletaId, Pageable pageable);
}
