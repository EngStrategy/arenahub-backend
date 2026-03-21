package com.engstrategy.arenahub_api.dto.agendamento;

import com.engstrategy.arenahub_api.dto.avaliacao.AvaliacaoDetalhesDTO;
import com.engstrategy.arenahub_api.dto.quadra.SlotHorarioResponseDTO;
import com.engstrategy.arenahub_api.model.enums.StatusAgendamento;
import com.engstrategy.arenahub_api.model.enums.TipoEsporte;
import com.engstrategy.arenahub_api.util.LocalTimeSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioInicio;
    @JsonSerialize(using = LocalTimeSerializer.class)
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
    private String nomePagadorPix;
    private String telefonePagadorPix;
    private String dataExpiracaoBloqueio;
}
