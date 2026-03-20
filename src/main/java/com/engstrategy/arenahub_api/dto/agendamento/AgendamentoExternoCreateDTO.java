package com.engstrategy.arenahub_api.dto.agendamento;

import com.engstrategy.arenahub_api.model.enums.TipoEsporte;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    private UUID atletaExistenteId;

    @Valid
    private NovoAtletaExternoDTO novoAtleta;

    @AssertTrue (message = "Deve ser fornecido um atleta existente ou um novo atleta, mas não ambos.")
    private boolean isAtletaValido() {
        return (atletaExistenteId != null && novoAtleta == null) || (novoAtleta != null && atletaExistenteId == null);
    }

}
