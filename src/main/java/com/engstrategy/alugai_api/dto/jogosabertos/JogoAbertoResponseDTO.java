package com.engstrategy.alugai_api.dto.jogosabertos;

import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class JogoAbertoResponseDTO {
    private Long agendamentoId;

    private LocalDate data;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioFim;

    private Integer vagasDisponiveis;
    private TipoEsporte esporte;
    private String nomeArena;
    private String nomeQuadra;
    private String cidade;
    private String urlFotoArena;
}
