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
import com.engstrategy.alugai_api.service.AgendamentoFixoService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/arena/agendamentos")
@Tag(name = "Arena Agendamentos", description = "Endpoints para gerenciamento de agendamentos pela arena")
@Validated
@RequiredArgsConstructor
public class ArenaAgendamentoController {
    private final AgendamentoService agendamentoService;
    private final AgendamentoFixoService agendamentoFixoService;
    private final AgendamentoMapper agendamentoMapper;

    @GetMapping
    @Operation(summary = "Listar cards mestre de agendamentos da arena (Agendamentos normais + Próxima Recorrência)", security = @SecurityRequirement(name = "bearerAuth"))
    @Transactional(readOnly = true)
    public ResponseEntity<Page<AgendamentoArenaResponseDTO>> listarAgendamentosArena(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Número da página (iniciando em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação (ex: dataAgendamento)")
            @RequestParam(defaultValue = "data_agendamento") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Data de início do filtro (opcional, formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data de fim do filtro (opcional, formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(description = "Status do agendamento (PENDENTE, FINALIZADO, etc.)")
            @RequestParam(required = false) StatusAgendamento status,
            @Parameter(description = "ID da quadra (opcional)")
            @RequestParam(required = false) Long quadraId) {

        UUID arenaId = userDetails.getUserId();

        Pageable pageableSemSort = PageRequest.of(page, size);

        Page<Agendamento> agendamentosPage = agendamentoService.buscarCardsMestrePorArenaId(
                arenaId,
                dataInicio,
                dataFim,
                quadraId,
                status,
                pageableSemSort);

        Page<AgendamentoArenaResponseDTO> response = agendamentosPage.map(
                agendamentoMapper::fromAgendamentoToArenaResponseDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/fixo/{agendamentoFixoId}/filhos")
    @Operation(summary = "Listar todos os agendamentos individuais de uma recorrência fixa (para o Drawer)", security = @SecurityRequirement(name = "bearerAuth"))
    @Transactional(readOnly = true)
    public ResponseEntity<List<AgendamentoArenaResponseDTO>> listarAgendamentosFixosFilhos(
            @Parameter(description = "ID do Agendamento Fixo (pai da recorrência)")
            @PathVariable Long agendamentoFixoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID arenaId = userDetails.getUserId();

        List<Agendamento> filhos = agendamentoService.buscarAgendamentosFixosFilhos(agendamentoFixoId, arenaId);

        List<AgendamentoArenaResponseDTO> response = filhos.stream()
                .map(agendamentoMapper::fromAgendamentoToArenaResponseDTO)
                .collect(Collectors.toList());

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

        UUID arenaId = userDetails.getUserId();
        Agendamento agendamentoAtualizado = agendamentoService.atualizarStatus(agendamentoId, arenaId, request.getStatus());
        AgendamentoArenaResponseDTO response = agendamentoMapper.fromAgendamentoToArenaResponseDTO(agendamentoAtualizado);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pendentes-resolucao")
    @Operation(summary = "Listar agendamentos pendentes que exigem ação da arena", security = @SecurityRequirement(name = "bearerAuth"))
    @Transactional(readOnly = true)
    public ResponseEntity<List<AgendamentoArenaResponseDTO>> listarAgendamentosPendentes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID arenaId = userDetails.getUserId();

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

    @DeleteMapping("/fixo/{agendamentoFixoId}")
    @Operation(summary = "Cancelar todos os agendamentos futuros de uma recorrência (por Agendamento Fixo ID)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recorrência cancelada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Arena não tem permissão"),
            @ApiResponse(responseCode = "404", description = "Recorrência não encontrada")
    })
    public ResponseEntity<Void> cancelarRecorrencia(
            @Parameter(description = "ID do Agendamento Fixo (pai da recorrência)", required = true)
            @PathVariable Long agendamentoFixoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID arenaId = userDetails.getUserId();
        agendamentoFixoService.cancelarAgendamentoFixoPorArena(agendamentoFixoId, arenaId);

        return ResponseEntity.noContent().build();
    }
}
