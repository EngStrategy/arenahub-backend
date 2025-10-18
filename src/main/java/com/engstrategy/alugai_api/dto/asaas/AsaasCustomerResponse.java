package com.engstrategy.alugai_api.dto.asaas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AsaasCustomerResponse {
    private String id;
    private String name;
    private String email;
    private String cpfCnpj;
}
