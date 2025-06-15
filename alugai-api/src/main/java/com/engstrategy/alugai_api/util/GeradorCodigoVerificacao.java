package com.engstrategy.alugai_api.util;

import com.engstrategy.alugai_api.model.CodigoVerificacao;

import java.time.LocalDateTime;
import java.util.Random;

public class GeradorCodigoVerificacao {
    public static CodigoVerificacao gerarCodigoVerificacao(String arenaEmail) {
        String code = String.format("%06d", new Random().nextInt(999999));
        return CodigoVerificacao.builder()
                .code(code)
                .email(arenaEmail)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
    }
}
