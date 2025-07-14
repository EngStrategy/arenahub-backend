package com.engstrategy.alugai_api.dto.jogosabertos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GerenciarSolicitacaoDTO {
    @NotNull
    private boolean aceitar;
}