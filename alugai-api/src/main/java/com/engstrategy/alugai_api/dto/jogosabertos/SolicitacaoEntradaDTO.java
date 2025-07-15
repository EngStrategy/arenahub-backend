package com.engstrategy.alugai_api.dto.jogosabertos;

import com.engstrategy.alugai_api.model.enums.StatusSolicitacao;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolicitacaoEntradaDTO {
    private Long id;
    private Long agendamentoId;
    private Long solicitanteId;
    private String nomeSolicitante;
    private String telefoneSolicitante;
    private String fotoSolicitante;
    private StatusSolicitacao status;
}