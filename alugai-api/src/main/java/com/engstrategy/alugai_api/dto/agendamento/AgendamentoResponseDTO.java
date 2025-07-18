package com.engstrategy.alugai_api.dto.agendamento;

import com.engstrategy.alugai_api.dto.quadra.SlotHorarioResponseDTO;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import com.engstrategy.alugai_api.util.LocalTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoResponseDTO {
    private Long id;
    private LocalDate dataAgendamento;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioInicio;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioFim;
    private BigDecimal valorTotal;
    private TipoEsporte esporte;
    private StatusAgendamento status;
    private Integer numeroJogadoresNecessarios;
    private List<SlotHorarioResponseDTO> slotsHorario;
    private Long quadraId;
    private String nomeQuadra;
    private String nomeArena;
    private String urlFotoQuadra;
    private String urlFotoArena;
    private boolean fixo;
    private boolean publico;
    private boolean possuiSolicitacoes;
}
