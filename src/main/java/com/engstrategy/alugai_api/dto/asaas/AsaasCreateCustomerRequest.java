package com.engstrategy.alugai_api.dto.asaas;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AsaasCreateCustomerRequest {
    private String name;
    private String email;
    private String phone;
    private String cpfCnpj;
}