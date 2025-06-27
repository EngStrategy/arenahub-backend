package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.esportes.EsporteResponseDTO;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/esportes")
@Tag(name = "Esportes", description = "Endpoints para recuperação de esportes")
@RequiredArgsConstructor
public class EsportesController {

    @GetMapping
    @Operation(summary = "Listar todos os esportes", description = "Retorna uma lista com todos os esportes disponíveis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de esportes retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EsporteResponseDTO.class)))
    })
    public ResponseEntity<List<EsporteResponseDTO>> listarEsportes() {
        List<EsporteResponseDTO> esportes = Arrays.stream(TipoEsporte.values())
                .map(esporte -> EsporteResponseDTO.builder()
                        .nome(esporte.name())
                        .apelido(esporte.getApelido())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(esportes);
    }
}
