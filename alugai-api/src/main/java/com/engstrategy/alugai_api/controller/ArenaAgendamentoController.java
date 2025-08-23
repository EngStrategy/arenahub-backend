package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoExternoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoResponseDTO;
import com.engstrategy.alugai_api.dto.agendamento.AtualizarStatusAgendamentoDTO;
import com.engstrategy.alugai_api.dto.agendamento.arena.AgendamentoArenaResponseDTO;
import com.engstrategy.alugai_api.exceptions.AccessDeniedException;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.mapper.AgendamentoMapper;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/arena/agendamentos")
@Tag(name = "Arena Agendamentos", description = "Endpoints para gerenciamento de agendamentos pela arena")
@Validated
@RequiredArgsConstructor
public class ArenaAgendamentoController {
    private final AgendamentoService agendamentoService;
    private final AgendamentoMapper agendamentoMapper;

    @GetMapping
    @Operation(summary = "Listar todos os agendamentos da arena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<AgendamentoArenaResponseDTO>> listarAgendamentosArena(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Número da página (iniciando em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação (ex: dataAgendamento)")
            @RequestParam(defaultValue = "dataAgendamento") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "desc") String direction,
            @Parameter(description = "Data de início do filtro (opcional, formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data de fim do filtro (opcional, formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(description = "Status do agendamento (opcional)")
            @RequestParam(required = false) StatusAgendamento status,
            @Parameter(description = "ID da quadra (opcional)")
            @RequestParam(required = false) Long quadraId) {

        Long arenaId = userDetails.getUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<Agendamento> agendamentosPage = agendamentoService.buscarPorArenaId(
                arenaId, dataInicio, dataFim, status, quadraId, pageable);

        Page<AgendamentoArenaResponseDTO> response = agendamentosPage.map(
                agendamentoMapper::fromAgendamentoToArenaResponseDTO);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{agendamentoId}/status")
    @Operation(summary = "Atualizar status de um agendamento", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do agendamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou mudança de status não permitida"),
            @ApiResponse(responseCode = "403", description = "Arena não tem permissão para alterar este agendamento"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    public ResponseEntity<AgendamentoArenaResponseDTO> atualizarStatusAgendamento(
            @Parameter(description = "ID do agendamento a ser atualizado", required = true)
            @PathVariable Long agendamentoId,
            @Valid @RequestBody AtualizarStatusAgendamentoDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long arenaId = userDetails.getUserId();
        Agendamento agendamentoAtualizado = agendamentoService.atualizarStatus(agendamentoId, arenaId, request.getStatus());
        AgendamentoArenaResponseDTO response = agendamentoMapper.fromAgendamentoToArenaResponseDTO(agendamentoAtualizado);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pendentes-resolucao")
    @Operation(summary = "Listar agendamentos pendentes que exigem ação da arena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AgendamentoArenaResponseDTO>> listarAgendamentosPendentes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long arenaId = userDetails.getUserId();

        List<Agendamento> agendamentosPendentes = agendamentoService.buscarPendentesAcaoPorArenaId(arenaId);

        List<AgendamentoArenaResponseDTO> response = agendamentosPendentes.stream()
                .map(agendamentoMapper::fromAgendamentoToArenaResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/externo")
    @Operation(summary = "Criar novo agendamento externo (pela Arena)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamentoExterno(
            @RequestBody @Valid AgendamentoExternoCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Garante que quem está logado é uma Arena
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ATLETA"))) {
            throw new AccessDeniedException("Apenas arenas podem criar agendamentos externos.");
        }

        Agendamento agendamento = agendamentoService.criarAgendamentoExterno(dto, userDetails.getUserId());
        AgendamentoResponseDTO response = agendamentoMapper.fromAgendamentoToResponseDTO(agendamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
