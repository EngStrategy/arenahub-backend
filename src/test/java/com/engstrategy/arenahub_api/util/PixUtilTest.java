package com.engstrategy.arenahub_api.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PixUtilTest {

    @Test
    void testGeneratePayloadWithAccents() {
        String chave = "12345678901";
        String nome = "José da Silva";
        String cidade = "Quixadá";
        String txid = "TX123";
        BigDecimal valor = new BigDecimal("100.00");

        String payload = PixUtil.generatePayload(chave, nome, cidade, txid, valor);

        // Verify that accents are removed
        assertFalse(payload.contains("José"));
        assertFalse(payload.contains("Quixadá"));
        assertTrue(payload.contains("Jose"));
        assertTrue(payload.contains("Quixada"));
    }
}
