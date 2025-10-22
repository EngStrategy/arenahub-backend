package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.subscription.AssinaturaDetalhesDTO;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.enums.Role;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.service.SubscriptionService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PriceRetrieveParams;
import com.stripe.param.SubscriptionListParams;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.engstrategy.alugai_api.model.enums.StatusAssinatura;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final ArenaRepository arenaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final EmailService emailService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public String createCheckoutSession(String priceId, CustomUserDetails userDetails) {
        Arena arena = arenaRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada."));

        String customerId = arena.getStripeCustomerId();
        if (customerId == null) {
            try {
                CustomerCreateParams customerParams = CustomerCreateParams.builder()
                        .setName(arena.getNome())
                        .setEmail(arena.getEmail())
                        .putMetadata("arena_id", arena.getId().toString())
                        .build();
                Customer customer = Customer.create(customerParams);
                customerId = customer.getId();
                arena.setStripeCustomerId(customerId);
                arenaRepository.save(arena);
            } catch (StripeException e) {
                log.error("Erro da API do Stripe ao criar cliente: {}", e.getMessage());
                throw new RuntimeException("Erro ao criar cliente no Stripe", e);
            }
        }

        try {
            com.stripe.param.checkout.SessionCreateParams.Builder paramsBuilder = com.stripe.param.checkout.SessionCreateParams.builder()
                    .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customerId)
                    .addLineItem(com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                            .setPrice(priceId)
                            .setQuantity(1L)
                            .build())
                    .setSuccessUrl(frontendUrl + "/subscricao/sucesso")
                    .setCancelUrl(frontendUrl + "/perfil/arena/assinatura?status=cancelado");

            // Periodo de teste de 30 dias
            paramsBuilder.setSubscriptionData(
                    com.stripe.param.checkout.SessionCreateParams.SubscriptionData.builder()
                            .setTrialPeriodDays(30L)
                            .build()
            );

            RequestOptions requestOptions = RequestOptions.RequestOptionsBuilder.unsafeSetStripeVersionOverride(RequestOptions.builder(), "2023-10-16").build();

            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(paramsBuilder.build(), requestOptions);
            return session.getId();
        } catch (StripeException e) {
            log.error("Erro da API do Stripe ao criar sessão de checkout: {}", e.getMessage());
            throw new RuntimeException("Erro ao criar sessão de checkout no Stripe", e.getCause());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssinaturaDetalhesDTO> getMinhaAssinatura(CustomUserDetails userDetails) {
        Arena arena = arenaRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada."));

        if (arena.getStripeCustomerId() == null) {
            return new ArrayList<>();
        }

        try {
            // Busca TODAS as assinaturas do cliente, expandindo a ÚLTIMA FATURA
            SubscriptionListParams listParams = SubscriptionListParams.builder()
                    .setCustomer(arena.getStripeCustomerId())
                    .setStatus(SubscriptionListParams.Status.ALL)
                    .addExpand("data.latest_invoice") // Pedimos para o Stripe incluir o objeto da última fatura
                    .build();

            List<Subscription> todasAsAssinaturas = Subscription.list(listParams).getData();

            if (todasAsAssinaturas.isEmpty()) {
                return new ArrayList<>();
            }

            // Filtra a lista para manter apenas as relevantes
            List<String> statusRelevantes = List.of("active", "trialing", "past_due");
            List<Subscription> assinaturasRelevantes = todasAsAssinaturas.stream()
                    .filter(sub -> statusRelevantes.contains(sub.getStatus()))
                    .collect(Collectors.toList());


            // Itera sobre a lista filtrada e extrair os detalhes
            return assinaturasRelevantes.stream().map(sub -> {
                        try {
                            // Buscamos o preço e o nome do plano da mesma forma
                            String priceId = sub.getItems().getData().get(0).getPrice().getId();
                            PriceRetrieveParams priceParams = PriceRetrieveParams.builder()
                                    .addExpand("product")
                                    .build();
                            Price price = Price.retrieve(priceId, priceParams, null);
                            Product product = price.getProductObject();
                            String planoNome = price.getProductObject().getName();
                            BigDecimal valor = BigDecimal.valueOf(price.getUnitAmount()).divide(new BigDecimal("100"));

                            Long proximaCobrancaTimestamp = sub.getTrialEnd() != null ? sub.getTrialEnd() : sub.getLatestInvoiceObject().getPeriodEnd();
                            LocalDate proximaCobranca = Instant.ofEpochSecond(proximaCobrancaTimestamp).atZone(ZoneId.systemDefault()).toLocalDate();

                            Integer limiteQuadras = null;
                            if (product.getMetadata().containsKey("limite_quadras")) {
                                try {
                                    limiteQuadras = Integer.parseInt(product.getMetadata().get("limite_quadras"));
                                } catch (NumberFormatException e) {
                                    log.error("Metadado 'limite_quadras' para o produto {} não é um número válido.", product.getId());
                                }
                            }

                            // Pega a última fatura que expandimos na chamada inicial
                            Invoice latestInvoice = sub.getLatestInvoiceObject();
                            if (latestInvoice == null) {
                                throw new IllegalStateException("A assinatura não possui uma fatura para determinar a próxima cobrança.");
                            }


                            StatusAssinatura statusFinal;
                            LocalDate dataCancelamento = null;

                            if (Boolean.TRUE.equals(sub.getCancelAtPeriodEnd()) && "active".equals(sub.getStatus())) {
                                statusFinal = StatusAssinatura.CANCELADA;
                                dataCancelamento = Instant.ofEpochSecond(sub.getCancelAt()).atZone(ZoneId.systemDefault()).toLocalDate();
                            } else {
                                statusFinal = fromStripeStatus(sub.getStatus());
                            }

                            return AssinaturaDetalhesDTO.builder()
                                    .status(statusFinal)
                                    .planoNome(planoNome)
                                    .proximaCobranca(proximaCobranca)
                                    .valor(valor)
                                    .dataCancelamento(dataCancelamento)
                                    .limiteQuadras(limiteQuadras)
                                    .build();

                        } catch (Exception e) {
                            log.error("Erro ao processar os detalhes da assinatura {}: {}", sub.getId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (StripeException e) {
            log.error("Erro ao buscar a lista de assinaturas no Stripe para o customerId {}: {}", arena.getStripeCustomerId(), e.getMessage());
            throw new RuntimeException("Erro ao comunicar com o provedor de pagamento.", e);
        }
    }

    @Override
    public String createCustomerPortalSession(CustomUserDetails userDetails) {
        Arena arena = arenaRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada."));

        if (arena.getStripeCustomerId() == null) {
            throw new IllegalStateException("Esta arena não possui uma assinatura para gerenciar.");
        }

        try {
            String returnUrl = frontendUrl + "/perfil/arena/assinatura";

            com.stripe.param.billingportal.SessionCreateParams params =
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(arena.getStripeCustomerId())
                            .setReturnUrl(returnUrl)
                            .build();

            com.stripe.model.billingportal.Session portalSession = com.stripe.model.billingportal.Session.create(params);
            return portalSession.getUrl();

        } catch (StripeException e) {
            log.error("Erro ao criar sessão do portal do cliente para o customerId {}: {}", arena.getStripeCustomerId(), e.getMessage());
            throw new RuntimeException("Erro ao comunicar com o provedor de pagamento.", e);
        }
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Falha na verificação da assinatura do webhook do Stripe.", e);
            throw new RuntimeException("Assinatura do webhook inválida", e);
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);

        if (stripeObject == null) {
            log.error("Falha ao deserializar o objeto do evento do Stripe: {}", event.getId());
            return;
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                log.info("Recebido evento checkout.session.completed!");
                com.stripe.model.checkout.Session session = (com.stripe.model.checkout.Session) stripeObject;
                handleCheckoutSessionCompleted(session);
                break;

            case "payment_intent.succeeded":
                log.info("Recebido evento payment_intent.succeeded!");
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                handlePaymentIntentSucceeded(paymentIntent);
                break;

            default:
                log.warn("Evento não tratado do Stripe recebido: {}", event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(com.stripe.model.checkout.Session session) {
        String customerId = session.getCustomer();
        Arena arena = arenaRepository.findByStripeCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada para o Stripe Customer ID: " + customerId));

        arena.setStatusAssinatura(StatusAssinatura.ATIVA);
        arenaRepository.save(arena);
        log.info("Assinatura da Arena ID {} (Stripe ID {}) atualizada para ATIVA.", arena.getId(), customerId);
    }

    private StatusAssinatura fromStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active", "trialing" -> // Um período de teste é considerado uma assinatura ativa
                    StatusAssinatura.ATIVA;
            case "past_due", "unpaid" -> StatusAssinatura.ATRASADA;
            case "canceled" -> StatusAssinatura.CANCELADA;
            default -> StatusAssinatura.INATIVA;
        };
    }

    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        String agendamentoIdStr = paymentIntent.getMetadata().get("agendamento_id");
        if (agendamentoIdStr == null) {
            log.warn("Recebido payment_intent.succeeded sem agendamento_id nos metadatas. ID: {}", paymentIntent.getId());
        }

        Long agendamentoId = Long.parseLong(agendamentoIdStr);

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado para o PaymentIntent: " + paymentIntent.getId()));

        // Altera o status do agendamento para PAGO se estiver AGUARDANDO_PAGAMENTO, para evitar reprocessamento
        if (agendamento.getStatus() == StatusAgendamento.AGUARDANDO_PAGAMENTO) {
            agendamento.setStatus(StatusAgendamento.PAGO);
            agendamentoRepository.save(agendamento);

            // Envia os e-mails de confirmação
            emailService.enviarEmailAgendamento(agendamento.getAtleta().getEmail(), agendamento.getAtleta().getNome(), agendamento, Role.ATLETA);
            emailService.enviarEmailAgendamento(agendamento.getQuadra().getArena().getEmail(), agendamento.getQuadra().getArena().getNome(), agendamento, Role.ARENA);

            log.info("Agendamento ID {} atualizado para PAGO via webhook do PaymentIntent.", agendamento.getId());
        }
    }
}