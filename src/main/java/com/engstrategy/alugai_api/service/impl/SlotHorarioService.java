package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.DuracaoReserva;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusDisponibilidade;
import com.engstrategy.alugai_api.model.enums.StatusIntervalo;
import com.engstrategy.alugai_api.repository.SlotHorarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SlotHorarioService {

    private final SlotHorarioRepository slotHorarioRepository;

    // Gera slots de horário para um intervalo específico baseado na duração da reserva
    public List<SlotHorario> gerarSlotsParaIntervalo(IntervaloHorario intervaloHorario, DuracaoReserva duracaoReserva) {
        List<SlotHorario> slots = new ArrayList<>();

        LocalTime inicio = intervaloHorario.getInicio();
        LocalTime fim = intervaloHorario.getFim();
        int minutosSlot = getDuracaoEmMinutos(duracaoReserva);

        if (minutosSlot <= 0) {
            return slots; // Evita divisão por zero ou loop infinito
        }

        LocalTime horarioAtual = inicio;

        while (horarioAtual.isBefore(fim)) {
            LocalTime proximoHorario = horarioAtual.plusMinutes(minutosSlot);

            // A verificação interna para evitar ultrapassar o 'fim' em intervalos normais
            if (proximoHorario.isAfter(fim) && proximoHorario.isAfter(horarioAtual)) {
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

            // Guardamos o horário antigo antes de atualizar
            LocalTime horarioAnterior = horarioAtual;
            horarioAtual = proximoHorario;

            // Se o novo horário for "menor" que o anterior, significa que cruzamos a meia-noite.
            // Neste caso, devemos parar o loop para evitar a repetição infinita.
            if (horarioAtual.isBefore(horarioAnterior)) {
                break;
            }
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

    // Verifica se os slots são subsequentes (para validação de agendamento)
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

    int getDuracaoEmMinutos(DuracaoReserva duracao) {
        switch (duracao) {
            case TRINTA_MINUTOS:
                return 30;
            case UMA_HORA:
                return 60;
            case UMA_HORA_E_MEIA:
                return 90;
            case DUAS_HORAS:
                return 120;
            default:
                throw new IllegalArgumentException("Duração não reconhecida: " + duracao);
        }
    }

    private StatusDisponibilidade mapearStatusDisponibilidade(StatusIntervalo statusIntervalo) {
        switch (statusIntervalo) {
            case DISPONIVEL:
                return StatusDisponibilidade.DISPONIVEL;
            case MANUTENCAO:
                return StatusDisponibilidade.MANUTENCAO;
            default:
                return StatusDisponibilidade.INDISPONIVEL;
        }
    }

    /**
     * Busca a sequência de slots correspondente a um horário de início e duração para uma dada quadra.
     * @param quadraId ID da quadra.
     * @param horarioInicio Hora de início da aula.
     * @param duracao Duração total da aula.
     * @return Set de SlotHorario que compõem a aula.
     */
    @Transactional(readOnly = true)
    public Set<SlotHorario> buscarSlotsPorHorarioDuracao(
            Long quadraId,
            LocalTime horarioInicio,
            DuracaoReserva duracao) {

        // Determina o horário de fim da reserva (que é o fim do último slot)
        int minutosDuracao = getDuracaoEmMinutos(duracao);
        LocalTime horarioFimReserva = horarioInicio.plusMinutes(minutosDuracao);

        List<SlotHorario> slotsDaQuadra = slotHorarioRepository
                .findByIntervaloHorario_HorarioFuncionamento_Quadra_Id(quadraId);

        // Filtra os slots que estão dentro do período [horarioInicio, horarioFimReserva)
        Set<SlotHorario> slotsSelecionados = slotsDaQuadra.stream()
                .filter(slot ->
                        !slot.getHorarioInicio().isBefore(horarioInicio) &&
                                !slot.getHorarioFim().isAfter(horarioFimReserva)
                )
                .collect(Collectors.toSet());


        // Verifica se os slots estão vazios
        if (slotsSelecionados.isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("Nenhum slot encontrado para a Quadra %d, iniciando às %s com duração de %s.",
                            quadraId, horarioInicio, duracao.name())
            );
        }

        // Converte para List e Ordena por horário de início
        List<SlotHorario> slotsOrdenados = slotsSelecionados.stream()
                .sorted(Comparator.comparing(SlotHorario::getHorarioInicio))
                .collect(Collectors.toList());

        // Verifica se a sequência de slots cobre a duração exata
        LocalTime primeiroSlotInicio = slotsOrdenados.get(0).getHorarioInicio();
        LocalTime ultimoSlotFim = slotsOrdenados.get(slotsOrdenados.size() - 1).getHorarioFim();

        if (!primeiroSlotInicio.equals(horarioInicio) || !ultimoSlotFim.equals(horarioFimReserva)) {
            throw new IllegalArgumentException(
                    String.format("Os slots encontrados não cobrem exatamente o intervalo de %s a %s.",
                            horarioInicio.format(DateTimeFormatter.ofPattern("HH:mm")),
                            horarioFimReserva.format(DateTimeFormatter.ofPattern("HH:mm")))
            );
        }

        return slotsSelecionados;
    }
}
