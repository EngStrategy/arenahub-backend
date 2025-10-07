package com.engstrategy.alugai_api.dto.agendamento;

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

}
