package com.engstrategy.alugai_api.dto.agendamento;

import com.engstrategy.alugai_api.dto.avaliacao.AvaliacaoDetalhesDTO;
import com.engstrategy.alugai_api.dto.quadra.SlotHorarioResponseDTO;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgendamentoResponseDTO {
    private Long id;
    private String dataAgendamento;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioInicio;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioFim;
    private BigDecimal valorTotal;
    private TipoEsporte esporte;
    private StatusAgendamento status;
    private Integer numeroJogadoresNecessarios;
    private Set<SlotHorarioResponseDTO> slotsHorario;
    private Long quadraId;
    private String nomeQuadra;
    private String nomeArena;
    private String urlFotoQuadra;
    private String urlFotoArena;
    private boolean fixo;
    private Long agendamentoFixoId;
    private boolean publico;
    private boolean possuiSolicitacoes;
    private AvaliacaoDetalhesDTO avaliacao;
    private Boolean avaliacaoDispensada;
}
