package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.DuracaoReserva;
import com.engstrategy.alugai_api.model.enums.MaterialEsportivo;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para criação de uma quadra")
public class QuadraCreateDTO {

    @Schema(description = "Nome da quadra", example = "Quadra 1", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nomeQuadra;

    @Schema(description = "URL da foto da quadra", example = "https://exemplo.com/foto-quadra.jpg")
    @URL(message = "URL da foto deve ser válida")
    private String urlFotoQuadra;

    @Schema(description = "Tipos de esporte suportados pela quadra", example = "[\"FUTEBOL_SOCIETY\", \"FUTSAL\"]", required = true)
    @NotEmpty(message = "Pelo menos um tipo de esporte deve ser informado")
    private List<TipoEsporte> tipoQuadra;

    @Schema(description = "Descrição da quadra", example = "Quadra coberta para futebol society e futsal")
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @Schema(description = "Duração da reserva", example = "UMA_HORA", required = true)
    @NotNull(message = "Duração da reserva é obrigatória")
    private DuracaoReserva duracaoReserva;

    @Schema(description = "Indica se a quadra possui cobertura", example = "true")
    private boolean cobertura;

    @Schema(description = "Indica se a quadra possui iluminação noturna", example = "true")
    private boolean iluminacaoNoturna;

    @Schema(description = "Materiais esportivos fornecidos", example = "[\"BOLA\", \"COLETE\"]")
    private List<MaterialEsportivo> materiaisFornecidos;

    @Schema(description = "ID da arena associada", example = "1", required = true)
    @NotNull(message = "ID da arena é obrigatório")
    private Long arenaId;

    @Schema(description = "Horários de funcionamento da quadra", required = true)
    @Valid
    private List<HorarioFuncionamentoCreateDTO> horariosFuncionamento;
}