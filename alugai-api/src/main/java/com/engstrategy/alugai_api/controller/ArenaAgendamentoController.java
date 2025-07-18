package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.agendamento.arena.AgendamentoArenaResponseDTO;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.mapper.AgendamentoMapper;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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
}
