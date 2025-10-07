package com.engstrategy.alugai_api.dto.asaas;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AsaasCreatePaymentRequest {
    private String customer;

    @Builder.Default
    private final String billingType = "PIX";
    private BigDecimal value;
    private String dueDate; // Formato yyyy-MM-dd
    private String description;

    @Builder.Default
    private final Integer daysAfterDueDateToRegistrationCancellation = 1;
}