package com.engstrategy.alugai_api.dto.jogosabertos;

import com.engstrategy.alugai_api.model.enums.StatusSolicitacao;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SolicitacaoEntradaDTO {
    private Long id;
    private Long agendamentoId;
    private UUID solicitanteId;
    private String nomeSolicitante;
    private String telefoneSolicitante;
    private String fotoSolicitante;
    private StatusSolicitacao status;
}