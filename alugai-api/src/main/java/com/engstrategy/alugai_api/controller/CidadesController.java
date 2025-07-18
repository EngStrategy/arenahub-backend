package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.cidades.CidadesResponseDTO;
import com.engstrategy.alugai_api.mapper.CidadesMapper;
import com.engstrategy.alugai_api.service.CidadesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cidades")
@Tag(name = "Cidades", description = "Endpoints para recuperação de cidades cadastradas no sistema")
@RequiredArgsConstructor
public class CidadesController {

    private final CidadesService cidadesService;
    private final CidadesMapper cidadesMapper;

    @GetMapping
    @Operation(summary = "Listar cidades", description = "Retorna uma lista paginada de cidades")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de cidades retornada com sucesso")
    })
    public ResponseEntity<Page<CidadesResponseDTO>> listarCidades(
            @Parameter(description = "Número da página (iniciando em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Direção da ordenação (asc/desc). A ordenação é sempre pelo nome da cidade.")
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "endereco.cidade"));

        Page<String> cidadesPage = cidadesService.listarCidades(pageable);
        Page<CidadesResponseDTO> response = cidadesPage.map(cidadesMapper::toCidadesResponseDTO);

        return ResponseEntity.ok(response);
    }
}
