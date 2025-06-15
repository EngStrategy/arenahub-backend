package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.model.CodigoVerificacao;
import com.engstrategy.alugai_api.model.VerificacaoRequest;
import com.engstrategy.alugai_api.service.impl.CodigoVerificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Verificação", description = "Endpoints para verificação de email")
public class CodigoVerificacaoController {
    private final CodigoVerificacaoService codigoVerificacaoService;

    @Operation(summary = "Verificar código de confirmação", description = "Valida o código de verificação enviado por email e ativa a conta do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email confirmado com sucesso ou já confirmado"),
            @ApiResponse(responseCode = "400", description = "Código expirado ou inválido"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping("/verify")
    @Transactional
    public ResponseEntity<String> verifyCode(@RequestBody VerificacaoRequest request) {
        CodigoVerificacao code = codigoVerificacaoService.getCode(request.getCode(), request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Código inválido"));

        if (code.getConfirmedAt() != null) {
            return ResponseEntity.ok("Email já confirmado");
        }

        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Código expirado");
        }

        codigoVerificacaoService.confirmCode(code);
        return ResponseEntity.ok("Email confirmado com sucesso");
    }
}
