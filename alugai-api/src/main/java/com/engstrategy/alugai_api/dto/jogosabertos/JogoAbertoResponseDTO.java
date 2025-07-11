package com.engstrategy.alugai_api.dto.jogosabertos;

import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class JogoAbertoResponseDTO {
    private Long agendamentoId;
    private LocalDate data;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private Integer vagasDisponiveis;
    private TipoEsporte esporte;
    private BigDecimal valorPorPessoa;
    private String nomeArena;
    private String nomeQuadra;
    private String cidade;
    private String urlFotoQuadra;
}
