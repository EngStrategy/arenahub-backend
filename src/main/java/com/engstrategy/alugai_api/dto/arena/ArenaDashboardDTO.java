package com.engstrategy.alugai_api.dto.arena;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoDashboardDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArenaDashboardDTO {
    private String nomeArena;
    private BigDecimal receitaDoMes;
    private Double percentualReceitaVsMesAnterior;
    private int agendamentosHoje;
    private int novosClientes;
    private Integer diferencaNovosClientesVsSemanaAnterior;
    private List<AgendamentoDashboardDTO> proximosAgendamentos;
    private Double taxaOcupacaoHoje;
}
