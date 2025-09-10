package com.engstrategy.alugai_api.dto.subscription;

import com.engstrategy.alugai_api.model.enums.StatusAssinatura;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssinaturaDetalhesDTO {
    private StatusAssinatura status;
    private String planoNome;
    private LocalDate proximaCobranca;
    private BigDecimal valor;
    private LocalDate dataCancelamento;
    private Integer limiteQuadras;
}