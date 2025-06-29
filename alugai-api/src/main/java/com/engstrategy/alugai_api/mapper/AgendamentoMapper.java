package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoResponseDTO;
import com.engstrategy.alugai_api.model.Agendamento;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoMapper {

    public AgendamentoResponseDTO toResponseDTO(Agendamento agendamento) {
        return AgendamentoResponseDTO.builder()
                .id(agendamento.getId())
                .dataAgendamento(agendamento.getDataAgendamento())
                .inicio(agendamento.getInicio())
                .fim(agendamento.getFim())
                .status(agendamento.getStatus())
                .quadraId(agendamento.getQuadra().getId())
                .nomeQuadra(agendamento.getQuadra().getNomeQuadra())
                .atletaId(agendamento.getAtleta().getId())
                .nomeAtleta(agendamento.getAtleta().getNome())
                .isPublico(!agendamento.isPrivado())
                .numeroJogadoresNecessarios(agendamento.getNumeroJogadoresNecessarios())
                .build();
    }
}