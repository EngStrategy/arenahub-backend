package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.aula.AulaCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoFixoResponseDTO;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.mapper.AgendamentoFixoMapper;
import com.engstrategy.alugai_api.model.AgendamentoFixo;
import com.engstrategy.alugai_api.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/arena/aulas")
@Tag(name = "Arena Aulas", description = "Endpoints para gerenciamento de ofertas de aulas pela Arena")
@Validated
@RequiredArgsConstructor
public class AulaController {

    private final AgendamentoService agendamentoService;
    private final AgendamentoFixoMapper agendamentoFixoMapper;

    @PostMapping
    @Operation(summary = "   Cadastrar nova oferta de Aula (Agendamento Fixo)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Aula cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou conflito de horário"),
            @ApiResponse(responseCode = "403", description = "Usuário não tem permissão de Arena"),
            @ApiResponse(responseCode = "404", description = "Quadra ou Instrutor não encontrados")
    })
    public ResponseEntity<AgendamentoFixoResponseDTO> cadastrarAula(
            @Valid @RequestBody AulaCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID proprietarioArenaId = userDetails.getUserId();

        AgendamentoFixo aulaSalva = agendamentoService.cadastrarAula(dto, proprietarioArenaId);

        AgendamentoFixoResponseDTO response = agendamentoFixoMapper.fromAgendamentoFixoToResponseDTO(aulaSalva);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
