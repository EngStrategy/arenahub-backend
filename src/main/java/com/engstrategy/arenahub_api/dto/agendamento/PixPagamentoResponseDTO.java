package com.engstrategy.arenahub_api.dto.agendamento;

import com.engstrategy.arenahub_api.model.enums.TipoChavePix;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PixPagamentoResponseDTO {
    private Long agendamentoId;
    private String statusAgendamento;
    private String qrCodeData;
    private String copiaECola;
    private String expiraEm;
    private TipoChavePix tipoChavePix;
    private Boolean pagamentoConfirmadoGateway;
}
