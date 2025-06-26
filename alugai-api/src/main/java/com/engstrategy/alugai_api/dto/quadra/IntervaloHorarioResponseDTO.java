package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.StatusIntervalo;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Dados de um intervalo de horário na resposta")
public class IntervaloHorarioResponseDTO {

    @Schema(description = "ID do intervalo", example = "1")
    private Long id;

    @Schema(description = "Hora de início do intervalo", example = "08:00")
    private LocalTime inicio;

    @Schema(description = "Hora de fim do intervalo", example = "12:00")
    private LocalTime fim;

    @Schema(description = "Valor da reserva para o intervalo", example = "100.00")
    private BigDecimal valor;

    @Schema(description = "Status do intervalo", example = "DISPONIVEL")
    private StatusIntervalo status;
}
