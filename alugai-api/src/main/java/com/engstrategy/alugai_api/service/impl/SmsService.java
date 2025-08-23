package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.zenvia.ContentDTO;
import com.engstrategy.alugai_api.dto.zenvia.ZenviaSmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);
    private static final String ZENVIA_API_URL = "https://api.zenvia.com/v2/channels/sms/messages";

    private final RestTemplate restTemplate;
    private final String zenviaApiToken;
    private final String senderId;

    public SmsService(RestTemplate restTemplate,
                      @Value("${zenvia.api-token}") String zenviaApiToken,
                      @Value("${zenvia.sender-id}") String senderId) {
        this.restTemplate = restTemplate;
        this.zenviaApiToken = zenviaApiToken;
        this.senderId = senderId;
    }

    @Async
    public void enviarSms(String para, String mensagem) {
        log.info("Enviando requisição para Zenvia com token terminando em: ...{}",
                zenviaApiToken.length() > 4 ? zenviaApiToken.substring(zenviaApiToken.length() - 4) : "TOKEN_MUITO_CURTO");

        try {
            String numeroDestino = "55" + para.replaceAll("\\D", "");

            // Monta o cabeçalho (Header) da requisição
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-TOKEN", zenviaApiToken);

            // Monta o corpo (Body) da requisição
            ZenviaSmsRequest requestBody = new ZenviaSmsRequest(
                    senderId,
                    numeroDestino,
                    Collections.singletonList(new ContentDTO("text", mensagem))
            );

            // Junta o corpo e o cabeçalho
            HttpEntity<ZenviaSmsRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            // Envia a requisição POST para a API V2
            String response = restTemplate.postForObject(ZENVIA_API_URL, requestEntity, String.class);

            log.info("Resposta da API Zenvia V2: {}", response);
            log.info("SMS para {} enviado para a fila da Zenvia.", numeroDestino);

        } catch (Exception e) {
            log.error("Falha ao enviar SMS via Zenvia para o número {}: {}", para, e.getMessage());
        }
    }
}