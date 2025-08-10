package com.engstrategy.alugai_api.dto.agendamento;

import com.engstrategy.alugai_api.model.enums.PeriodoAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoCreateDTO {
    @NotNull
    private Long quadraId;
    @NotNull
    private LocalDate dataAgendamento;
    @NotNull
    private List<Long> slotHorarioIds;
    @NotNull
    private TipoEsporte esporte;
    @JsonProperty("isFixo")
    private boolean isFixo;
    @JsonProperty("isPublico")
    private boolean isPublico;
    private PeriodoAgendamento periodoFixo;
    private Integer numeroJogadoresNecessarios;
}
