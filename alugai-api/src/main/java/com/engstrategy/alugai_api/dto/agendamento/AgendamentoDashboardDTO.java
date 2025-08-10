package com.engstrategy.alugai_api.dto.agendamento;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class AgendamentoDashboardDTO {
    private Long agendamentoId;
    private String clienteNome;
    private String urlFoto;
    private String quadraNome;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private String clienteTelefone;
}
