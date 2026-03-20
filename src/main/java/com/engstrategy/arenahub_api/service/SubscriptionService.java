package com.engstrategy.arenahub_api.service;

import com.engstrategy.arenahub_api.dto.subscription.AssinaturaDetalhesDTO;
import com.engstrategy.arenahub_api.jwt.CustomUserDetails;
import com.engstrategy.arenahub_api.model.Agendamento;

import java.util.List;

public interface SubscriptionService {
    String createCheckoutSession(String priceId, CustomUserDetails userDetails);
    void handleStripeWebhook(String payload, String sigHeader);
    List<AssinaturaDetalhesDTO> getMinhaAssinatura(CustomUserDetails userDetails);
    String createCustomerPortalSession(CustomUserDetails userDetails);
    String createAgendamentoPaymentIntent(Agendamento agendamento);
}
