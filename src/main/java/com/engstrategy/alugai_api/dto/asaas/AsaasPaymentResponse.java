package com.engstrategy.alugai_api.dto.asaas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AsaasPaymentResponse {
    private String id;
    private String status;
    private BigDecimal value;

    @JsonProperty("qrCode")
    private String pixQrCode;

    @JsonProperty("payload")
    private String pixData;

    private Long expiresAt;

    private PixResponse pix;

    @Getter
    @Setter
    public static class PixResponse {
        private String encodedImage; // O QR Code em Base64
        private String payload; // O código Pix Copia e Cola
        private Long expiresAt; // Timestamp de expiração
    }
}
