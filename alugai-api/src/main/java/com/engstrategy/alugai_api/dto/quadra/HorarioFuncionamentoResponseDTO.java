package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados de um horário de funcionamento na resposta")
public class HorarioFuncionamentoResponseDTO {

    @Schema(description = "ID do horario de funcionamento", example = "1")
    private Long id;

    @Schema(description = "Dia da semana", example = "SEGUNDA")
    private DiaDaSemana diaDaSemana;

    @Schema(description = "Lista de intervalos de horário")
    private List<IntervaloHorarioResponseDTO> intervalosDeHorario;
}
