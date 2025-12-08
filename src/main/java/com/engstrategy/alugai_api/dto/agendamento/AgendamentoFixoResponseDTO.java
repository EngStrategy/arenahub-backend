package com.engstrategy.alugai_api.dto.agendamento;

import com.engstrategy.alugai_api.dto.aula.InstrutorDetalhesDTO;
import com.engstrategy.alugai_api.model.enums.PeriodoAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamentoFixo;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoFixoResponseDTO {
    @NotNull
    private Long id;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dataInicio;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dataFim;
    @NotNull
    private PeriodoAgendamento periodo;
    @NotNull
    private StatusAgendamentoFixo status;

    private int totalAgendamentos;

    private List<AgendamentoResponseDTO> agendamentos;
    @NotNull
    private UUID atletaId;

    // * Quando for uma AULA
    private String nomeAula;
    private Integer limiteAtletas;
    private boolean isElegivelWellhub;
    private InstrutorDetalhesDTO instrutor;
    private BigDecimal valorBaseMensal;
    private BigDecimal valorPlanoTrimestral;
    private BigDecimal valorPlanoSemestral;
}
