package com.engstrategy.arenahub_api.dto.agendamento;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InformarPagadorPixDTO {

    @NotBlank(message = "O nome completo do pagador é obrigatório")
    private String nomeCompletoPagador;
}
