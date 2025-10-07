package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.asaas.AsaasWebhookEvent;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.enums.Role;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
import com.engstrategy.alugai_api.service.impl.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asaas/webhook")
public class AsaasWebhookController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private EmailService emailService;

    @PostMapping("/cobranca-status")
    @Transactional
    public ResponseEntity<?> handleAsaasStatus(@RequestBody AsaasWebhookEvent event) {

        if ("PAYMENT_RECEIVED".equals(event.getEvent()) ||
                "PAYMENT_CONFIRMED".equals(event.getEvent()) ||
                "PAYMENT_UPDATED".equals(event.getEvent())) {

            String pagamentoStatus = event.getPayment().getStatus();

            if ("CONFIRMED".equals(pagamentoStatus) || "RECEIVED".equals(pagamentoStatus)) {

                String asaasPaymentId = event.getPayment().getId();

                // Usa o método de busca que carrega todas as relações
                agendamentoRepository.findByAsaasPaymentIdFetchRelations(asaasPaymentId)
                        .ifPresent(agendamento -> {
                            if (agendamento.getStatus() != StatusAgendamento.PAGO) {

                                agendamento.setStatus(StatusAgendamento.PAGO);
                                agendamentoRepository.save(agendamento);

                                // 1. Extração das entidades carregadas
                                Atleta atleta = agendamento.getAtleta();
                                Quadra quadra = agendamento.getQuadra();

                                // 2. Notificação do Atleta e Arena
                                emailService.enviarEmailAgendamento(
                                        atleta.getEmail(),
                                        atleta.getNome(),
                                        agendamento,
                                        Role.ATLETA
                                );
                                emailService.enviarEmailAgendamento(
                                        quadra.getArena().getEmail(),
                                        quadra.getArena().getNome(),
                                        agendamento,
                                        Role.ARENA
                                );
                            }
                        });
            }
        }

        // O Asaas espera um retorno 200 OK
        return ResponseEntity.ok().build();
    }
}