package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.subscription.AssinaturaDetalhesDTO;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Stripe", description = "Endpoints para gerenciamento do Stripe das Arena")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String priceId = payload.get("priceId");
        String sessionId = subscriptionService.createCheckoutSession(priceId, userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);

        log.info("Sessão de checkout criada com sucesso para o usuário: {}", userDetails.getUsername() + " com Price ID: " + priceId +
                " e Session ID: " + sessionId + ".");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar detalhes da assinatura da arena logada", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AssinaturaDetalhesDTO>> getMinhaAssinatura(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<AssinaturaDetalhesDTO> assinatura = subscriptionService.getMinhaAssinatura(userDetails);
        return ResponseEntity.ok(assinatura);
    }

    @PostMapping("/customer-portal")
    @Operation(summary = "Criar sessão para o portal do cliente Stripe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> createCustomerPortalSession(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String portalUrl = subscriptionService.createCustomerPortalSession(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("url", portalUrl);

        return ResponseEntity.ok(response);
    }
}
