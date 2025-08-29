package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoResponseDTO;
import com.engstrategy.alugai_api.dto.avaliacao.AvaliacaoDTO;
import com.engstrategy.alugai_api.dto.avaliacao.AvaliacaoResponseDTO;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.mapper.AgendamentoMapper;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.AgendamentoFixo;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoAgendamento;
import com.engstrategy.alugai_api.service.AgendamentoFixoService;
import com.engstrategy.alugai_api.service.AgendamentoService;
import com.engstrategy.alugai_api.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/agendamentos")
@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final AgendamentoMapper agendamentoMapper;
    private final AgendamentoFixoService agendamentoFixoService;
    private final AvaliacaoService avaliacaoService;

    @PostMapping
    @Operation(summary = "Criar novo agendamento", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento(
            @RequestBody @Valid AgendamentoCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        UUID atletaId = customUserDetails.getUserId();

        try {
            Agendamento agendamento = agendamentoService.criarAgendamento(dto, atletaId);

            // Se for agendamento fixo, criar os recorrentes
            if (dto.isFixo()) {
                AgendamentoFixo agendamentoFixo = agendamentoFixoService.criarAgendamentosFixos(agendamento);
                log.info("Agendamento fixo criado com ID: {}", agendamentoFixo.getId());
            }

            AgendamentoResponseDTO response = agendamentoMapper.fromAgendamentoToResponseDTO(agendamento);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao criar agendamento: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar agendamento normal", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> cancelarAgendamento(@PathVariable(name = "id") Long id,
                                                    @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        agendamentoService.cancelarAgendamento(id, customUserDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/fixo/{agendamentoFixoId}")
    @Operation(summary = "Cancelar agendamento fixo", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> cancelarAgendamentoFixo(
            @PathVariable Long agendamentoFixoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        try {
            agendamentoFixoService.cancelarAgendamentoFixo(agendamentoFixoId, customUserDetails.getUserId());
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao cancelar agendamento fixo: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/meus-agendamentos")
    @Operation(summary = "Listar meus agendamentos", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<AgendamentoResponseDTO>> getMeusAgendamentos(
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
            @Parameter(description = "Tipo de agendamento (NORMAL, FIXO, AMBOS)")
            @RequestParam(defaultValue = "AMBOS") TipoAgendamento tipoAgendamento,
            @Parameter(description = "Status do agendamento (opcional)")
            @RequestParam(required = false) StatusAgendamento status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<Agendamento> agendamentosPage = agendamentoService.buscarPorAtletaId(
                userDetails.getUserId(),
                dataInicio,
                dataFim,
                tipoAgendamento,
                status,
                pageable);
        Page<AgendamentoResponseDTO> response = agendamentosPage.map(agendamentoMapper::fromAgendamentoToResponseDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{agendamentoId}/avaliacoes")
    @Operation(summary = "Cria ou dispensa uma avaliação para um agendamento", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AvaliacaoResponseDTO> criarOuDispensarAvaliacao(
            @PathVariable Long agendamentoId,
            @RequestBody @Valid AvaliacaoDTO avaliacaoDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Optional<AvaliacaoResponseDTO> resultado = avaliacaoService.criarOuDispensarAvaliacao(agendamentoId, avaliacaoDTO, userDetails);

        return resultado
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto)) // Se a avaliação foi criada
                .orElse(ResponseEntity.noContent().build()); // Se a avaliação foi dispensada
    }

    @PutMapping("/avaliacoes/{avaliacaoId}")
    @Operation(summary = "Atualiza uma avaliação existente", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AvaliacaoResponseDTO> atualizarAvaliacao(
            @PathVariable Long avaliacaoId,
            @RequestBody @Valid AvaliacaoDTO avaliacaoDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AvaliacaoResponseDTO avaliacaoAtualizada = avaliacaoService.atualizarAvaliacao(avaliacaoId, avaliacaoDTO, userDetails);
        return ResponseEntity.ok(avaliacaoAtualizada);
    }

    @GetMapping("/avaliacoes-pendentes")
    @Operation(summary = "Listar meus agendamentos com avaliação pendente", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AgendamentoResponseDTO>> getAgendamentosParaAvaliar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<Agendamento> agendamentos = agendamentoService.buscarAgendamentosParaAvaliacao(userDetails.getUserId());

        List<AgendamentoResponseDTO> response = agendamentos.stream()
                .map(agendamentoMapper::fromAgendamentoToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
