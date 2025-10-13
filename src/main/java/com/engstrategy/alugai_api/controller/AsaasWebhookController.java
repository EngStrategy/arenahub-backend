package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.asaas.AsaasWebhookEvent;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.enums.Role;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
import com.engstrategy.alugai_api.service.AgendamentoFixoService;
import com.engstrategy.alugai_api.service.impl.EmailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asaas/webhook")
@Slf4j
@Tag(name = "Asaas Webhook", description = "Endpoints para receber notificações do Asaas")
public class AsaasWebhookController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AgendamentoFixoService agendamentoFixoService;

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
                            log.info("WEBHOOK: Agendamento recebido ID: {} | Status Atual: {} | É Fixo: {}",
                                    agendamento.getId(),
                                    agendamento.getStatus(),
                                    agendamento.isFixo());

                            if (agendamento.getStatus() != StatusAgendamento.PAGO) {

                                agendamento.setStatus(StatusAgendamento.PAGO);
                                agendamentoRepository.save(agendamento);

                                Atleta atleta = agendamento.getAtleta();
                                Quadra quadra = agendamento.getQuadra();

                                if (agendamento.isFixo()) {
                                    log.info("WEBHOOK: Acionando a criação de recorrências para o ID: {}", agendamento.getId());
                                    // Esta chamada cria as instâncias semanais e persiste no banco.
                                    agendamentoFixoService.criarAgendamentosFixos(agendamento);
                                }

                                // Notificação do Atleta e Arena
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