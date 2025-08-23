package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.StatusIntervalo;
import com.engstrategy.alugai_api.util.LocalTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados de um intervalo de horário na resposta")
public class IntervaloHorarioResponseDTO {

    @Schema(description = "ID do intervalo", example = "1")
    private Long id;

    @Schema(description = "Hora de início do intervalo", example = "08:00")
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime inicio;

    @Schema(description = "Hora de fim do intervalo", example = "12:00")
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime fim;

    @Schema(description = "Valor da reserva para o intervalo", example = "100.00")
    private BigDecimal valor;

    @Schema(description = "Status do intervalo", example = "DISPONIVEL")
    private StatusIntervalo status;

    @Builder.Default
    @Schema(description = "Slots de horário", example = "08:00 - 09:00")
    private List<SlotHorarioResponseDTO> slotsDisponiveis = new ArrayList<>();
}
