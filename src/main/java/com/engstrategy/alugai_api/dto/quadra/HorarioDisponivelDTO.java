package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.StatusIntervalo;
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
public class HorarioDisponivelDTO {

    private Long intervaloId;
    private LocalTime inicio;
    private LocalTime fim;
    private BigDecimal valor;
    private boolean disponivel;
}