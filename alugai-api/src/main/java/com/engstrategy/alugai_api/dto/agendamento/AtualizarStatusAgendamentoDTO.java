package com.engstrategy.alugai_api.dto.agendamento;

import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Dados para atualização do status de um agendamento pela arena")
public class AtualizarStatusAgendamentoDTO {

    @NotNull(message = "O novo status é obrigatório")
    @Schema(description = "Novo status do agendamento", example = "PAGO", required = true,
            allowableValues = {"PAGO", "AUSENTE", "CANCELADO"})
    private StatusAgendamento status;
}
