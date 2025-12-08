package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.StatusDisponibilidade;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioFim;

    private BigDecimal valor;
    private StatusDisponibilidade statusDisponibilidade;
}
