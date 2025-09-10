package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stripe")
@Tag(name = "Webhooks", description = "Endpoints para receber eventos de webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/webhook")
    @Operation(summary = "Receber eventos do webhook do Stripe", description = "Este endpoint é para uso exclusivo do Stripe.")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Webhook do Stripe recebido!");
        try {
            subscriptionService.handleStripeWebhook(payload, sigHeader);
        } catch (RuntimeException e) {
            log.error("Erro ao processar webhook do Stripe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}