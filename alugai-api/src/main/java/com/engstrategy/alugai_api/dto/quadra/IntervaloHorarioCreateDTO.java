package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.StatusIntervalo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para criação de um intervalo de horário")
public class IntervaloHorarioCreateDTO {

    @Schema(description = "Hora de início do intervalo", example = "08:00", required = true)
    @NotNull(message = "Hora de início é obrigatória")
    private LocalTime inicio;

    @Schema(description = "Hora de fim do intervalo", example = "12:00", required = true)
    @NotNull(message = "Hora de fim é obrigatória")
    private LocalTime fim;

    @Schema(description = "Valor da reserva para o intervalo", example = "100.00", required = true)
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @Schema(description = "Status do intervalo", example = "DISPONIVEL", required = true)
    @NotNull(message = "Status é obrigatório")
    private StatusIntervalo status;
}
