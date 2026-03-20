package com.engstrategy.arenahub_api.dto.quadra;

import com.engstrategy.arenahub_api.model.enums.StatusDisponibilidade;
import com.engstrategy.arenahub_api.util.LocalTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
public class SlotHorarioResponseDTO {

    private Long id;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioInicio;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioFim;

    private BigDecimal valor;
    private StatusDisponibilidade statusDisponibilidade;
}
