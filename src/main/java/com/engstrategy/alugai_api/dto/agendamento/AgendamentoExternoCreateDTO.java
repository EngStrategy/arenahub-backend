package com.engstrategy.alugai_api.dto.agendamento;

import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AgendamentoExternoCreateDTO {

    @NotNull
    private Long quadraId;

    @NotNull
    @FutureOrPresent
    private LocalDate dataAgendamento;

    @NotEmpty
    private List<Long> slotHorarioIds;

    private TipoEsporte esporte;

    private Long atletaExistenteId;

    @Valid
    private NovoAtletaExternoDTO novoAtleta;

    @AssertTrue (message = "Deve ser fornecido um atleta existente ou um novo atleta, mas não ambos.")
    private boolean isAtletaValido() {
        return (atletaExistenteId != null && novoAtleta == null) || (novoAtleta != null && atletaExistenteId == null);
    }

}
