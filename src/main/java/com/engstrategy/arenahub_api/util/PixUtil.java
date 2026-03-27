package com.engstrategy.arenahub_api.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Base64;
import java.util.regex.Pattern;

public class PixUtil {

    public static String generatePayload(String chave, String nomeRecebedor, String cidadeRecebedor, String txid, BigDecimal valor) {
        StringBuilder payload = new StringBuilder();

        nomeRecebedor = removeAccents(nomeRecebedor);
        cidadeRecebedor = removeAccents(cidadeRecebedor);

        // 00 - Payload Format Indicator
        payload.append(formatField("00", "01"));
        
        // 26 - Merchant Account Information - Pix
        StringBuilder merchantAccount = new StringBuilder();
        merchantAccount.append(formatField("00", "br.gov.bcb.pix"));
        merchantAccount.append(formatField("01", chave));
        payload.append(formatField("26", merchantAccount.toString()));

        // 52 - Merchant Category Code
        payload.append(formatField("52", "0000"));

        // 53 - Transaction Currency (986 = BRL)
        payload.append(formatField("53", "986"));

        // 54 - Transaction Amount
        if (valor != null) {
            payload.append(formatField("54", valor.setScale(2).toString()));
        }

        // 58 - Country Code
        payload.append(formatField("58", "BR"));

        // 59 - Merchant Name
        payload.append(formatField("59", truncate(nomeRecebedor, 25)));

        // 60 - Merchant City
        payload.append(formatField("60", truncate(cidadeRecebedor, 15)));

        // 62 - Additional Data Field
        if (txid != null && !txid.isEmpty()) {
            payload.append(formatField("62", formatField("05", txid)));
        } else {
             payload.append(formatField("62", formatField("05", "***")));
        }

        // 63 - CRC16 (Placeholder)
        payload.append("6304");
        payload.append(calculateCRC16(payload.toString()));

        return payload.toString();
    }

    private static String formatField(String id, String value) {
        return id + String.format("%02d", value.length()) + value;
    }

    private static String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.substring(0, Math.min(text.length(), maxLength));
    }

    public static String generateQrCodeBase64(String payload) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pngData);
    }

    private static String calculateCRC16(String payload) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        byte[] bytes = payload.getBytes(StandardCharsets.US_ASCII);

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }
}
