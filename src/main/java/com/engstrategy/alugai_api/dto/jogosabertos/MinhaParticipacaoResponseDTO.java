package com.engstrategy.alugai_api.dto.jogosabertos;

import com.engstrategy.alugai_api.model.enums.StatusSolicitacao;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import com.engstrategy.alugai_api.util.LocalTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class MinhaParticipacaoResponseDTO {
    private Long solicitacaoId; // ID da solicitação, para poder cancelar a participação
    private Long agendamentoId; // ID do agendamento original
    private String nomeArena;
    private String nomeQuadra;
    private String urlFotoArena;
    private LocalDate data;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioInicio;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioFim;

    private TipoEsporte esporte;
    private StatusSolicitacao status;
}