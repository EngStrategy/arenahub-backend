package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.DuracaoReserva;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusDisponibilidade;
import com.engstrategy.alugai_api.model.enums.StatusIntervalo;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
import com.engstrategy.alugai_api.repository.SlotHorarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SlotHorarioService {

    private final SlotHorarioRepository slotHorarioRepository;
    private final AgendamentoRepository agendamentoRepository;

    /**
     * Gera slots de horário para um intervalo específico baseado na duração da reserva
     */
    public List<SlotHorario> gerarSlotsParaIntervalo(IntervaloHorario intervaloHorario, DuracaoReserva duracaoReserva) {
        List<SlotHorario> slots = new ArrayList<>();

        LocalTime inicio = intervaloHorario.getInicio();
        LocalTime fim = intervaloHorario.getFim();
        int minutosSlot = getDuracaoEmMinutos(duracaoReserva);

        LocalTime horarioAtual = inicio;

        while (horarioAtual.isBefore(fim)) {
            LocalTime proximoHorario = horarioAtual.plusMinutes(minutosSlot);

            if (proximoHorario.isAfter(fim)) {
                break;
            }

            SlotHorario slot = SlotHorario.builder()
                    .horarioInicio(horarioAtual)
                    .horarioFim(proximoHorario)
                    .valor(intervaloHorario.getValor())
                    .statusDisponibilidade(mapearStatusDisponibilidade(intervaloHorario.getStatus()))
                    .intervaloHorario(intervaloHorario)
                    .build();

            slots.add(slot);
            horarioAtual = proximoHorario;
        }

        return slots;
    }

    public void gerarSlotsParaQuadra(Quadra quadra) {
        quadra.getHorariosFuncionamento().forEach(horarioFuncionamento -> {
            horarioFuncionamento.getIntervalosDeHorario().forEach(intervalo -> {
                List<SlotHorario> slots = gerarSlotsParaIntervalo(intervalo, quadra.getDuracaoReserva());
                intervalo.setSlotsHorario(slots);
            });
        });
    }

    /**
     * Verifica se os slots são subsequentes (para validação de agendamento)
     */
    public boolean saoSlotsSubsequentes(List<Long> slotIds) {
        if (slotIds.size() <= 1) return true;

        List<SlotHorario> slots = slotHorarioRepository.findAllById(slotIds);
        slots.sort(Comparator.comparing(SlotHorario::getHorarioInicio));

        for (int i = 0; i < slots.size() - 1; i++) {
            SlotHorario atual = slots.get(i);
            SlotHorario proximo = slots.get(i + 1);

            if (!atual.getHorarioFim().equals(proximo.getHorarioInicio())) {
                return false;
            }
        }

        return true;
    }

    public boolean verificarSlotOcupado(SlotHorario slot, LocalDate data) {
        return slot.getAgendamentos().stream()
                .anyMatch(agendamento ->
                        agendamento.getDataAgendamento().equals(data) &&
                                !agendamento.getStatus().equals(StatusAgendamento.CANCELADO));
    }

    public boolean verificarIntervaloTemSlotsOcupados(IntervaloHorario intervalo) {
        LocalDate hoje = LocalDate.now();
        return intervalo.getSlotsHorario().stream()
                .anyMatch(slot -> slot.getAgendamentos().stream()
                        .anyMatch(agendamento ->
                                agendamento.getDataAgendamento().isAfter(hoje) &&
                                        !agendamento.getStatus().equals(StatusAgendamento.CANCELADO)));
    }

    // Métodos auxiliares privados
    private int getDuracaoEmMinutos(DuracaoReserva duracao) {
        switch (duracao) {
            case TRINTA_MINUTOS: return 30;
            case UMA_HORA: return 60;
            case UMA_HORA_E_MEIA: return 90;
            case DUAS_HORAS: return 120;
            default: throw new IllegalArgumentException("Duração não reconhecida: " + duracao);
        }
    }

    private StatusDisponibilidade mapearStatusDisponibilidade(StatusIntervalo statusIntervalo) {
        switch (statusIntervalo) {
            case DISPONIVEL: return StatusDisponibilidade.DISPONIVEL;
            case MANUTENCAO: return StatusDisponibilidade.MANUTENCAO;
            default: return StatusDisponibilidade.INDISPONIVEL;
        }
    }
}
