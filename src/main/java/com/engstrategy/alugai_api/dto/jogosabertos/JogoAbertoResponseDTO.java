package com.engstrategy.alugai_api.dto.jogosabertos;

import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import com.engstrategy.alugai_api.util.LocalTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class JogoAbertoResponseDTO {
    private Long agendamentoId;

    private LocalDate data;

    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioInicio;

    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioFim;

    private Integer vagasDisponiveis;
    private TipoEsporte esporte;
    private String nomeArena;
    private String nomeQuadra;
    private String cidade;
    private String urlFotoArena;
}
