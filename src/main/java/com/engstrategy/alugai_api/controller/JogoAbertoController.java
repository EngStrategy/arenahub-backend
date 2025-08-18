package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.jogosabertos.GerenciarSolicitacaoDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.JogoAbertoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.MinhaParticipacaoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.SolicitacaoEntradaDTO;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.service.JogoAbertoService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jogos-abertos")
@Tag(name = "Jogos Abertos", description = "Endpoints para gerenciamento de jogos abertos")
@RequiredArgsConstructor
public class JogoAbertoController {

    private final JogoAbertoService jogoAbertoService;

    @GetMapping
    @Operation(summary = "Listar todos os jogos abertos com vagas", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<JogoAbertoResponseDTO>> listarJogosAbertos(
            @Parameter(description = "Número da página (iniciando em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação (ex: dataAgendamento)")
            @RequestParam(defaultValue = "dataAgendamento") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Filtrar por cidade")
            @RequestParam(required = false) String cidade,
            @Parameter(description = "Filtrar por esporte (valores possíveis: FUTEBOL_SOCIETY, FUTSAL, BEACHTENNIS, etc.)")
            @RequestParam(required = false) String esporte
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<JogoAbertoResponseDTO> jogosAbertos = jogoAbertoService.listarJogosAbertos(pageable, cidade, esporte);
        return ResponseEntity.ok(jogosAbertos);
    }

    @PostMapping("/{agendamentoId}/solicitar-entrada")
    @Operation(summary = "Solicitar entrada em um jogo aberto", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SolicitacaoEntradaDTO> solicitarEntrada(
            @PathVariable Long agendamentoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SolicitacaoEntradaDTO solicitacao = jogoAbertoService.solicitarEntrada(agendamentoId, userDetails.getUserId());
        return ResponseEntity.ok(solicitacao);
    }

    @GetMapping("/{agendamentoId}/solicitacoes")
    @Operation(summary = "Listar solicitações de um jogo criado por mim", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<SolicitacaoEntradaDTO>> listarSolicitacoes(
            @PathVariable Long agendamentoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<SolicitacaoEntradaDTO> solicitacoes = jogoAbertoService.listarSolicitacoes(agendamentoId, userDetails.getUserId());
        return ResponseEntity.ok(solicitacoes);
    }

    @PatchMapping("/solicitacoes/{solicitacaoId}")
    @Operation(summary = "Aceitar ou recusar uma solicitação de entrada", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SolicitacaoEntradaDTO> gerenciarSolicitacao(
            @PathVariable Long solicitacaoId,
            @Valid @RequestBody GerenciarSolicitacaoDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SolicitacaoEntradaDTO solicitacao = jogoAbertoService.gerenciarSolicitacao(solicitacaoId, userDetails.getUserId(), dto.isAceitar());
        return ResponseEntity.ok(solicitacao);
    }

    @DeleteMapping("/solicitacoes/{solicitacaoId}/sair")
    @Operation(summary = "Sair de um jogo aberto que fui aceito", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> sairDeJogo(
            @PathVariable Long solicitacaoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        jogoAbertoService.sairDeJogoAberto(solicitacaoId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/minhas-participacoes")
    @Operation(summary = "Lista todos os jogos abertos que o atleta logado solicitou para participar",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<MinhaParticipacaoResponseDTO>> listarMinhasParticipacoes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MinhaParticipacaoResponseDTO> participacoes = jogoAbertoService.
                listarMinhasParticipacoes(userDetails.getUserId());
        return ResponseEntity.ok(participacoes);
    }
}