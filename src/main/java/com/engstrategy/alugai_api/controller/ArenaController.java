package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.agendamento.arena.CidadeDTO;
import com.engstrategy.alugai_api.dto.arena.*;
import com.engstrategy.alugai_api.dto.usuario.AlterarSenhaRequest;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.mapper.ArenaMapper;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.service.ArenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/arenas")
@Tag(name = "Arenas", description = "Endpoints para gerenciamento de arenas")
@Validated
@AllArgsConstructor
public class ArenaController {

    private final ArenaService arenaService;
    private final ArenaMapper arenaMapper;

    @PostMapping
    @Operation(summary = "Criar arena", description = "Cria uma nova arena no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Arena criada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ArenaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "409", description = "Email, telefone, CPF ou CNPJ já cadastrado")
    })
    public ResponseEntity<ArenaResponseDTO> criarArena(@Valid @RequestBody ArenaCreateDTO arenaCreateDTO) {
        Arena arena = arenaMapper.mapArenaCreateDtoToArena(arenaCreateDTO);
        ArenaResponseDTO response = arenaMapper.mapArenaToArenaResponseDTO(arenaService.criarArena(arena));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar arena por ID", description = "Retorna os dados de uma arena específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arena encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ArenaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Arena não encontrada")
    })
    public ResponseEntity<ArenaResponseDTO> buscarArenaPorId(
            @Parameter(description = "ID da arena", required = true)
            @PathVariable UUID id) {

        ArenaResponseDTO response = arenaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar arenas", description = "Retorna uma lista paginada de arenas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de arenas retornada com sucesso")
    })
    public ResponseEntity<Page<ArenaResponseDTO>> listarArenas(
            @Parameter(description = "Número da página (iniciando em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "nome") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Filtrar por cidade")
            @RequestParam(required = false) String cidade,
            @Parameter(description = "Filtrar por esporte (valores possíveis: FUTEBOL_SOCIETY, FUTEBOL_SETE, FUTEBOL_ONZE, FUTSAL, BEACHTENIS, VOLEI, FUTEVOLEI, BASQUETE, HANDEBOL)")
            @RequestParam(required = false) String esporte,
            @Parameter(description = "Latitude do usuário para busca por proximidade")
            @RequestParam(required = false) Double latitude,
            @Parameter(description = "Longitude do usuário para busca por proximidade")
            @RequestParam(required = false) Double longitude,
            @Parameter(description = "Raio da busca em quilômetros (km)")
            @RequestParam(required = false) Double raioKm
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));

        Page<ArenaResponseDTO> response = arenaService.listarTodos(pageable, cidade, esporte, latitude, longitude, raioKm);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar arena", description = "Atualiza os dados de uma arena existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arena atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ArenaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Arena não encontrada")
    })
    public ResponseEntity<ArenaResponseDTO> atualizarArena(
            @Parameter(description = "ID da arena", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ArenaUpdateDTO arenaUpdateDTO) {

        Arena updatedArena = arenaService.atualizar(id, arenaUpdateDTO);
        ArenaResponseDTO response = arenaMapper.mapArenaToArenaResponseDTO(updatedArena);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir arena", description = "Remove uma arena do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Arena excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Arena não encontrada")
    })
    public ResponseEntity<Void> excluirArena(
            @Parameter(description = "ID da arena", required = true)
            @PathVariable UUID id) {
        arenaService.excluir(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/me/alterar-senha")
    @Operation(summary = "Alterar a senha da arena logada", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, senhas não coincidem ou senha atual incorreta"),
            @ApiResponse(responseCode = "404", description = "Arena não encontrada")
    })
    public ResponseEntity<String> alterarSenha(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AlterarSenhaRequest request) {

        arenaService.alterarSenha(userDetails.getUserId(), request.getSenhaAtual(), request.getNovaSenha());

        return ResponseEntity.ok("Senha alterada com sucesso.");
    }

    @GetMapping("/cidades")
    @Operation(summary = "Listar cidades", description = "Retorna uma lista de cidades e estados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de cidades retornada com sucesso")
    })
    public ResponseEntity<List<CidadeDTO>> getCidades() {
        List<CidadeDTO> cidades = arenaService.getCidades();
        return ResponseEntity.ok(cidades);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Resumo do Dashboard da Arena", description = "Retorna os dados do dashboard para a arena logada", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ArenaDashboardDTO> getDashboardData(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            ArenaDashboardDTO dashboardData = arenaService.getDashboardData(userDetails.getUserId());
            return ResponseEntity.ok(dashboardData);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
