package com.engstrategy.alugai_api.dto.jogosabertos;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class GerenciarSolicitacaoDTO {
    @NotNull
    private boolean aceitar;
}