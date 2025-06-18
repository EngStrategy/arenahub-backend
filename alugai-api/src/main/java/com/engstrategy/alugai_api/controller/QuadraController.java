package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.quadra.QuadraCreateDTO;
import com.engstrategy.alugai_api.dto.quadra.QuadraResponseDTO;
import com.engstrategy.alugai_api.dto.quadra.QuadraUpdateDTO;
import com.engstrategy.alugai_api.mapper.QuadraMapper;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.service.QuadraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quadras")
@Tag(name = "Quadras", description = "Endpoints para gerenciamento de quadras")
@Validated
@AllArgsConstructor
public class QuadraController {

    private final QuadraService quadraService;
    private final QuadraMapper quadraMapper;

    @PostMapping
    @Operation(summary = "Criar quadra", description = "Cria uma nova quadra no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quadra criada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuadraResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Arena não encontrada")
    })
    public ResponseEntity<QuadraResponseDTO> criarQuadra(@Valid @RequestBody QuadraCreateDTO quadraCreateDTO) {
        Quadra quadra = quadraMapper.mapQuadraCreateDtoToQuadra(quadraCreateDTO);
        QuadraResponseDTO response = quadraMapper.mapQuadraToQuadraResponseDTO(quadraService.criarQuadra(quadra, quadraCreateDTO.getArenaId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar quadra por ID", description = "Retorna os dados de uma quadra específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quadra encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuadraResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Quadra não encontrada")
    })
    public ResponseEntity<QuadraResponseDTO> buscarQuadraPorId(
            @Parameter(description = "ID da quadra", required = true)
            @PathVariable Long id) {
        Quadra quadra = quadraService.buscarPorId(id);
        QuadraResponseDTO response = quadraMapper.mapQuadraToQuadraResponseDTO(quadra);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar quadras", description = "Retorna uma lista paginada de quadras")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de quadras retornada com sucesso")
    })
    public ResponseEntity<Page<QuadraResponseDTO>> listarQuadras(
            @Parameter(description = "Número da página (iniciando em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "nomeQuadra") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Filtrar por arena ID")
            @RequestParam(required = false) Long arenaId,
            @Parameter(description = "Filtrar por esporte")
            @RequestParam(required = false) String esporte) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<Quadra> quadras = quadraService.listarTodos(pageable, arenaId, esporte);
        Page<QuadraResponseDTO> response = quadras.map(quadraMapper::mapQuadraToQuadraResponseDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar quadra", description = "Atualiza os dados de uma quadra existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quadra atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuadraResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Quadra não encontrada")
    })
    public ResponseEntity<QuadraResponseDTO> atualizarQuadra(
            @Parameter(description = "ID da quadra", required = true)
            @PathVariable Long id,
            @Valid @RequestBody QuadraUpdateDTO quadraUpdateDTO) {
        Quadra updatedQuadra = quadraService.atualizar(id, quadraUpdateDTO);
        QuadraResponseDTO response = quadraMapper.mapQuadraToQuadraResponseDTO(updatedQuadra);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{quadraId}/arena/{arenaId}")
    @Operation(summary = "Excluir quadra", description = "Remove uma quadra do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quadra excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Quadra não encontrada"),
            @ApiResponse(responseCode = "422", description = "Quadra não associada à arena especificada")
    })
    public ResponseEntity<Void> excluirQuadra(
            @Parameter(description = "ID da quadra", required = true)
            @PathVariable Long quadraId,
            @Parameter(description = "ID da arena", required = true)
            @PathVariable Long arenaId) {
        quadraService.excluir(quadraId, arenaId);
        return ResponseEntity.noContent().build();
    }
}
