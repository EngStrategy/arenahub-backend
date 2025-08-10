package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.exceptions.AlreadyConfirmedEmailException;
import com.engstrategy.alugai_api.exceptions.ExpiredConfirmationCodeException;
import com.engstrategy.alugai_api.exceptions.InvalidConfirmationCodeException;
import com.engstrategy.alugai_api.model.*;
import com.engstrategy.alugai_api.service.impl.CodigoVerificacaoService;
import com.engstrategy.alugai_api.service.impl.ResetSenhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final ResetSenhaService resetSenhaService;

    @Operation(summary = "Verificar código de confirmação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email confirmado com sucesso ou codigo já confirmado"),
            @ApiResponse(responseCode = "400", description = "Código expirado, inválido ou email já confirmado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping("/verify")
    @Transactional
    public ResponseEntity<String> verifyCode(@RequestBody VerificacaoRequest request) {
        CodigoVerificacao code = codigoVerificacaoService.getCode(request.getCode(), request.getEmail())
                .orElseThrow(() -> new InvalidConfirmationCodeException("Código inválido"));

        if (code.getConfirmedAt() != null) {
            throw new AlreadyConfirmedEmailException("Email já confirmado");
        }

        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredConfirmationCodeException("Código expirado");
        }

        codigoVerificacaoService.confirmCode(code);
        return ResponseEntity.ok("Email confirmado com sucesso");
    }

    @Operation(summary = "Reenviar código de verificação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código reenviado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Email não registrado, limite de reenvios excedido ou cooldown ativo"),
            @ApiResponse(responseCode = "400", description = "Email já confirmado")
    })
    @PostMapping("/resend-verification")
    @Transactional
    public ResponseEntity<String> resendVerificationCode(@RequestBody @Valid ResendVerificationRequest request) {
        codigoVerificacaoService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok("Código reenviado com sucesso");
    }

    @Operation(summary = "Solicitar reset de senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código enviado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Limite de reenvios excedido ou cooldown ativo")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> solicitarResetSenha(@RequestBody @Valid ForgotPasswordRequest request) {
        resetSenhaService.solicitarResetSenha(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Verificar código de reset de senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código válido"),
            @ApiResponse(responseCode = "400", description = "Código inválido ou expirado")
    })
    @PostMapping("/verify-reset-code")
    public ResponseEntity<Void> verificarResetCode(@RequestBody @Valid VerifyResetCodeRequest request) {
        resetSenhaService.verificarCodigoReset(request.getEmail(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Redefinir senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        resetSenhaService.redefinirSenha(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
