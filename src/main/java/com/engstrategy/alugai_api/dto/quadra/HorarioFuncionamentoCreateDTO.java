package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
@Schema(description = "Dados para criação de um horário de funcionamento")
public class HorarioFuncionamentoCreateDTO {

    @Schema(description = "Dia da semana", example = "SEGUNDA", required = true)
    @NotNull(message = "Dia da semana é obrigatório")
    private DiaDaSemana diaDaSemana;

    @Schema(description = "Lista de intervalos de horário", required = true)
    @NotEmpty(message = "Pelo menos um intervalo de horário deve ser informado")
    @Valid
    private List<IntervaloHorarioCreateDTO> intervalosDeHorario;
}
