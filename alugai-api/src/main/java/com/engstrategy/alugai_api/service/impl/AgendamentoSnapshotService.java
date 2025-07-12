package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AgendamentoSnapshotService {

    private final AgendamentoRepository agendamentoRepository;

    public void criarSnapshotsParaSlots(List<SlotHorario> slots) {
        for (SlotHorario slot : slots) {
            List<Agendamento> agendamentos = slot.getAgendamentos().stream()
                    .filter(this::devePreservarInformacoes)
                    .toList();

            for (Agendamento agendamento : agendamentos) {
                if (agendamento.getHorarioInicioSnapshot() == null) {
                    agendamento.criarSnapshot();
                }
            }
        }

        // Flush para garantir que os snapshots sejam salvos
        agendamentoRepository.flush();
    }

    private boolean devePreservarInformacoes(Agendamento agendamento) {
        return agendamento.getStatus() != StatusAgendamento.PENDENTE;
    }
}
