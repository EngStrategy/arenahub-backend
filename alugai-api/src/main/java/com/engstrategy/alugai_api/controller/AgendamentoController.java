package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoResponseDTO;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.mapper.AgendamentoMapper;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/agendamentos")
@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
@Validated
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final AgendamentoMapper agendamentoMapper;

    @PostMapping
    @Operation(summary = "Criar um novo agendamento", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento(
            @Valid @RequestBody AgendamentoCreateDTO agendamentoCreateDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Agendamento agendamento = agendamentoService.criarAgendamento(agendamentoCreateDTO, userDetails.getUserId());

        AgendamentoResponseDTO response = agendamentoMapper.toResponseDTO(agendamento);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/meus-agendamentos")
    @Operation(summary = "Listar meus agendamentos (Atleta)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<AgendamentoResponseDTO>> getMeusAgendamentos(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Número da página (iniciando em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação (ex: dataAgendamento)")
            @RequestParam(defaultValue = "dataAgendamento") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "desc") String direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));

        Page<Agendamento> agendamentosPage = agendamentoService.buscarPorAtletaId(userDetails.getUserId(), pageable);

        Page<AgendamentoResponseDTO> response = agendamentosPage.map(agendamentoMapper::toResponseDTO);

        return ResponseEntity.ok(response);
    }
}
