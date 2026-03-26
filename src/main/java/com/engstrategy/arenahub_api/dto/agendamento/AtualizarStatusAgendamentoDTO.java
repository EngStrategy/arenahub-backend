package com.engstrategy.arenahub_api.dto.agendamento;

import com.engstrategy.arenahub_api.model.enums.StatusAgendamento;
import com.engstrategy.arenahub_api.model.enums.FormaPagamentoAgendamento;
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

    @Schema(description = "Forma de pagamento (obrigatória se o status for PAGO)", example = "PIX")
    private FormaPagamentoAgendamento formaPagamento;
}
