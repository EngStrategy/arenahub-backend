package com.engstrategy.alugai_api.dto.agendamento;

import com.engstrategy.alugai_api.model.enums.PeriodoAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamentoFixo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoFixoResponseDTO {
    @NotNull
    private Long id;
    @NotNull
    private LocalDate dataInicio;
    @NotNull
    private LocalDate dataFim;
    @NotNull
    private PeriodoAgendamento periodo;
    @NotNull
    private StatusAgendamentoFixo status;
    @NotNull
    private int totalAgendamentos;
    @NotNull
    private List<AgendamentoResponseDTO> agendamentos;
    @NotNull
    private Long atletaId;
}
