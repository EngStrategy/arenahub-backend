package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para atualização de horários de funcionamento")
public class HorarioFuncionamentoUpdateDTO {

    @Schema(description = "Dia da semana", example = "SEGUNDA", required = true)
    @NotNull(message = "Dia da semana é obrigatório")
    private DiaDaSemana diaDaSemana;

    @Schema(description = "Lista de intervalos de horário")
    @Valid
    private List<IntervaloHorarioUpdateDTO> intervalosDeHorario;
}
