package com.engstrategy.alugai_api.dto.jogosabertos;

import com.engstrategy.alugai_api.model.enums.StatusSolicitacao;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate data;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioFim;

    private TipoEsporte esporte;
    private StatusSolicitacao status;
}