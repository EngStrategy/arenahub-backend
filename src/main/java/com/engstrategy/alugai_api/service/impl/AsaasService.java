//package com.engstrategy.alugai_api.service.impl;
//
//import com.engstrategy.alugai_api.dto.asaas.*;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.web.client.RestTemplateBuilder;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
//
//@Service
//public class AsaasService {
//
//    private final RestTemplate restTemplate;
//    private final String asaasUrl;
//    private final String asaasToken;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsaasService.class);
//
//    public AsaasService(
//            RestTemplateBuilder restTemplateBuilder,
//            @Value("${asaas.api.url}") String asaasUrl,
//            @Value("${asaas.api.token}") String asaasToken) {
//
//        this.asaasUrl = asaasUrl;
//        this.asaasToken = asaasToken;
//
//        this.restTemplate = restTemplateBuilder
//                .defaultHeader("access_token", asaasToken)
//                .build();
//    }
//
//    // Cria Cliente
//    public AsaasCustomerResponse createCustomer(AsaasCreateCustomerRequest request) {
//        String url = asaasUrl + "/customers";
//        try {
//            return restTemplate.postForObject(url, request, AsaasCustomerResponse.class);
//        } catch (HttpClientErrorException e) {
//            throw new RuntimeException("Erro ao criar cliente no Asaas: " + e.getResponseBodyAsString(), e);
//        }
//    }
//
//    // Cria cobrança PIX
//    public AsaasPaymentResponse createPixPayment(AsaasCreatePaymentRequest request) {
//        String url = asaasUrl + "/payments";
//
//        try {
//            String requestBody = new ObjectMapper().writeValueAsString(request);
//            log.info("Requisição Asaas para Pagamento: {}", requestBody);
//            // -----------------------------------------------------------
//
//            // Usamos postForEntity para ter controle total sobre a resposta
//            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
//
//            String responseBody = responseEntity.getBody();
//
//            if (responseBody != null) {
//                log.info("Resposta BRUTA do Asaas para Pagamento: {}", responseBody);
//            }
//
//            // Uso de ObjectMapper para tentar converter a string JSON bruta
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//            return mapper.readValue(responseBody, AsaasPaymentResponse.class);
//
//        } catch (HttpClientErrorException e) {
//            throw new RuntimeException("Erro ao criar pagamento PIX no Asaas: " + e.getMessage(), e);
//        } catch (Exception e) {
//            log.error("Erro no mapeamento da resposta do Asaas.", e);
//            throw new RuntimeException("Falha ao processar a resposta do provedor de pagamento.", e);
//        }
//    }
//
//    // --- Obtem dados do PIX ---
//    public AsaasPixQrCodeResponse getPixQrCode(String paymentId) {
//        String url = asaasUrl + "/payments/" + paymentId + "/pixQrCode";
//
//        try {
//            log.info("Buscando PIX QrCode para o pagamento ID: {}", paymentId);
//
//            // Usamos postForEntity para obter a string bruta (mais seguro)
//            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
//            String responseBody = responseEntity.getBody();
//
//            if (responseBody != null) {
//                log.info("Resposta BRUTA do Asaas para PIX QR Code: {}", responseBody);
//            }
//
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//            return mapper.readValue(responseBody, AsaasPixQrCodeResponse.class);
//
//        } catch (HttpClientErrorException e) {
//            log.error("Erro ao buscar dados PIX QrCode do Asaas ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw new RuntimeException("Erro ao obter QR Code PIX. " + e.getMessage(), e);
//        } catch (Exception e) {
//            log.error("Erro no mapeamento da resposta do PIX QR Code.", e);
//            throw new RuntimeException("Falha ao processar a resposta do provedor de pagamento (GET PIX).", e);
//        }
//    }
//}