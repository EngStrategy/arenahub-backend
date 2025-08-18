package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.atleta.AtletaCreateDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaResponseDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaUpdateDTO;
import com.engstrategy.alugai_api.dto.usuario.AlterarSenhaRequest;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.mapper.AtletaMapper;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.service.AtletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/atletas")
@Tag(name = "Atletas", description = "Endpoints para gerenciamento de atletas")
@Validated
@RequiredArgsConstructor
public class AtletaController {

    private final AtletaService atletaService;
    private final AtletaMapper atletaMapper;

    @PostMapping
    @Operation(summary = "Criar atleta", description = "Cria um novo atleta no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Atleta criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AtletaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "409", description = "Email ou telefone já cadastrado")
    })
    public ResponseEntity<AtletaResponseDTO> criarAtleta(@Valid @RequestBody AtletaCreateDTO atletaCreateDTO) {

        Atleta atleta = atletaMapper.mapAtletaCreateDtoToAtleta(atletaCreateDTO);
        AtletaResponseDTO response = atletaMapper.mapAtletaToAtletaResponseDto(atletaService.criarAtleta(atleta));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar atleta por ID", description = "Retorna os dados de um atleta específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atleta encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AtletaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Atleta não encontrado")
    })
    public ResponseEntity<AtletaResponseDTO> buscarAtletaPorId(
            @Parameter(description = "ID do atleta", required = true)
            @PathVariable Long id) {

        Atleta atleta = atletaService.buscarPorId(id);
        AtletaResponseDTO response = atletaMapper.mapAtletaToAtletaResponseDto(atleta);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar atletas", description = "Retorna uma lista paginada de atletas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de atletas retornada com sucesso")
    })
    public ResponseEntity<Page<AtletaResponseDTO>> listarAtletas(
            @Parameter(description = "Número da página (iniciando em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "nome") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<Atleta> atletas = atletaService.listarTodos(pageable);
        Page<AtletaResponseDTO> response = atletas.map(atletaMapper::mapAtletaToAtletaResponseDto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar atleta", description = "Atualiza os dados de um atleta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atleta atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AtletaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Atleta não encontrado")
    })
    public ResponseEntity<AtletaResponseDTO> atualizarAtleta(
            @Parameter(description = "ID do atleta", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AtletaUpdateDTO atletaUpdateDTO) {

        Atleta updatedAtleta = atletaService.atualizar(id, atletaUpdateDTO);
        AtletaResponseDTO response = atletaMapper.mapAtletaToAtletaResponseDto(updatedAtleta);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir atleta", description = "Remove um atleta do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Atleta excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Atleta não encontrado")
    })
    public ResponseEntity<Void> excluirAtleta(
            @Parameter(description = "ID do atleta", required = true)
            @PathVariable Long id) {
        atletaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/alterar-senha")
    @Operation(summary = "Alterar a senha do atleta logado", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            // A mensagem de erro 401 foi removida e a descrição do 400 foi atualizada
            @ApiResponse(responseCode = "400", description = "Dados inválidos, senhas não coincidem ou senha atual incorreta"),
            @ApiResponse(responseCode = "404", description = "Atleta não encontrado")
    })
    public ResponseEntity<String> alterarSenha(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AlterarSenhaRequest request) {

        atletaService.alterarSenha(userDetails.getUserId(), request.getSenhaAtual(), request.getNovaSenha());

        return ResponseEntity.ok("Senha alterada com sucesso.");
    }
}
