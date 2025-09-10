package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.subscription.AssinaturaDetalhesDTO;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;

import java.util.List;

public interface SubscriptionService {
    String createCheckoutSession(String priceId, CustomUserDetails userDetails);
    void handleStripeWebhook(String payload, String sigHeader);
    List<AssinaturaDetalhesDTO> getMinhaAssinatura(CustomUserDetails userDetails);
    String createCustomerPortalSession(CustomUserDetails userDetails);
}
