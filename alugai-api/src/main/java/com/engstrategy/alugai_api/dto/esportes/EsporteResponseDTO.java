package com.engstrategy.alugai_api.dto.esportes;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Resposta com dados de um esporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsporteResponseDTO {

    @Schema(description = "Nome do esporte no formato do enum", example = "FUTEBOL_SOCIETY")
    private String nome;

    @Schema(description = "Apelido formatado do esporte", example = "Futebol Society")
    private String apelido;
}
