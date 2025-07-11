package com.engstrategy.alugai_api.dto.quadra;

import com.engstrategy.alugai_api.model.enums.DuracaoReserva;
import com.engstrategy.alugai_api.model.enums.MaterialEsportivo;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta com dados da quadra")
public class QuadraResponseDTO {

    @Schema(description = "ID da quadra", example = "1")
    private Long id;

    @Schema(description = "Nome da quadra", example = "Quadra 1")
    private String nomeQuadra;

    @Schema(description = "URL da foto da quadra", example = "https://exemplo.com/foto-quadra.jpg")
    private String urlFotoQuadra;

    @Schema(description = "Tipos de esporte suportados pela quadra", example = "[\"FUTEBOL_SOCIETY\", \"FUTSAL\"]")
    private List<TipoEsporte> tipoQuadra;

    @Schema(description = "Descrição da quadra", example = "Quadra coberta para futebol society e futsal")
    private String descricao;

    @Schema(description = "Duração da reserva", example = "UMA_HORA")
    private DuracaoReserva duracaoReserva;

    @Schema(description = "Indica se a quadra possui cobertura", example = "true")
    private boolean cobertura;

    @Schema(description = "Indica se a quadra possui iluminação noturna", example = "true")
    private boolean iluminacaoNoturna;

    @Schema(description = "Materiais esportivos fornecidos", example = "[\"BOLA\", \"COLETE\"]")
    private List<MaterialEsportivo> materiaisFornecidos;

    @Schema(description = "ID da arena associada", example = "1")
    private Long arenaId;

    @Schema(description = "Nome da arena associada", example = "Arena Sports Center")
    private String nomeArena;

    @Schema(description = "Horários de funcionamento da quadra")
    private List<HorarioFuncionamentoResponseDTO> horariosFuncionamento;
}
