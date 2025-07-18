package com.engstrategy.alugai_api.dto.cidades;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Resposta com dados de uma cidade")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CidadesResponseDTO {

    @Schema(description = "Nome da Cidade", example = "Fortaleza")
    private String cidade;
}