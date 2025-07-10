package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoFixoResponseDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoResponseDTO;
import com.engstrategy.alugai_api.dto.quadra.SlotHorarioResponseDTO;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.*;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.repository.QuadraRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AgendamentoMapper {

    private final QuadraRepository quadraRepository;

    public Agendamento fromCreateToAgendamento(AgendamentoCreateDTO dto, List<SlotHorario> slots, Atleta atleta) {
        Quadra quadra = quadraRepository.findById(dto.getQuadraId())
                .orElseThrow(() -> new UserNotFoundException("Quadra não encontrada com ID: " + dto.getQuadraId()));

        return Agendamento.builder()
                .dataAgendamento(dto.getDataAgendamento())
                .esporte(dto.getEsporte())
                .isFixo(dto.isFixo())
                .isPublico(dto.isPublico())
                .periodoAgendamentoFixo(dto.getPeriodoFixo())
                .numeroJogadoresNecessarios(dto.getNumeroJogadoresNecessarios())
                .slotsHorario(slots)
                .status(StatusAgendamento.PENDENTE)
                .atleta(atleta)
                .quadra(quadra)
                .build();
    }

    public AgendamentoResponseDTO fromAgendamentoToResponseDTO(Agendamento agendamento) {
        return AgendamentoResponseDTO.builder()
                .id(agendamento.getId())
                .quadraId(agendamento.getQuadra().getId())
                .dataAgendamento(agendamento.getDataAgendamento())
                .horarioInicio(agendamento.getHorarioInicio())
                .horarioFim(agendamento.getHorarioFim())
                .valorTotal(agendamento.getValorTotal())
                .esporte(agendamento.getEsporte())
                .isFixo(agendamento.isFixo())
                .isPublico(agendamento.isPublico())
                .status(agendamento.getStatus())
                .numeroJogadoresNecessarios(agendamento.getNumeroJogadoresNecessarios())
                .slotsHorario(agendamento.getSlotsHorario().stream()
                        .map(this::mapearSlotParaResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private SlotHorarioResponseDTO mapearSlotParaResponse(SlotHorario slot) {
        return SlotHorarioResponseDTO.builder()
                .id(slot.getId())
                .horarioInicio(slot.getHorarioInicio())
                .horarioFim(slot.getHorarioFim())
                .valor(slot.getValor())
                .statusDisponibilidade(slot.getStatusDisponibilidade())
                .build();
    }

    public AgendamentoFixoResponseDTO fromAgendamentoFixoToResponseDTO(AgendamentoFixo agendamentoFixo) {
        return AgendamentoFixoResponseDTO.builder()
                .id(agendamentoFixo.getId())
                .dataInicio(agendamentoFixo.getDataInicio())
                .dataFim(agendamentoFixo.getDataFim())
                .periodo(agendamentoFixo.getPeriodo())
                .status(agendamentoFixo.getStatus())
                .totalAgendamentos(agendamentoFixo.getAgendamentos().size())
                .agendamentos(agendamentoFixo.getAgendamentos().stream()
                        .map(this::fromAgendamentoToResponseDTO)
                        .collect(Collectors.toList()))
                .atletaId(agendamentoFixo.getAtleta().getId())
                .build();
    }
}