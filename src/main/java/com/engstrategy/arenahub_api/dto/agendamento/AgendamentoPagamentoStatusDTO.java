package com.engstrategy.arenahub_api.dto.agendamento;

import com.engstrategy.arenahub_api.model.enums.StatusAgendamento;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgendamentoPagamentoStatusDTO {
    private StatusAgendamento status;
    private Boolean pagamentoConfirmadoGateway;
    private Boolean nomePagadorInformado;
}
