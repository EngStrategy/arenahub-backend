package com.engstrategy.alugai_api.dto.agendamento;

import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class AgendamentoResponseDTO {

    private Long id;
    private LocalDate dataAgendamento;
    private LocalTime inicio;
    private LocalTime fim;
    private StatusAgendamento status;
    private Long quadraId;
    private String nomeQuadra;
    private Long atletaId;
    private String nomeAtleta;
    private boolean isPublico;
    private Integer numeroJogadoresNecessarios;

}
