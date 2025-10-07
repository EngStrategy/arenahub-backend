package com.engstrategy.alugai_api.dto.asaas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasPixQrCodeResponse {

    @JsonProperty("encodedImage")
    private String qrCodeBase64;

    private String payload;

    @JsonProperty("expirationDate")
    private String expirationDate; // Tipo String, pois é "2026-09-26 23:59:59"
}