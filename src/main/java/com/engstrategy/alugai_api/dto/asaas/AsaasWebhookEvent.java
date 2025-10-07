package com.engstrategy.alugai_api.dto.asaas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasWebhookEvent {

    private String event; // Ex: "PAYMENT_RECEIVED", "PAYMENT_CONFIRMED"
    private String webhookId;

    private AsaasPaymentData payment;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AsaasPaymentData {
        private String id;
        private String customer;
        private String status; // Ex: "CONFIRMED", "RECEIVED", "PENDING"
    }
}
