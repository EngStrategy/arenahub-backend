package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.exceptions.*;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.CodigoResetSenha;
import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.repository.CodigoResetSenhaRepository;
import com.engstrategy.alugai_api.service.ArenaService;
import com.engstrategy.alugai_api.service.AtletaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ResetSenhaService {
    private final CodigoResetSenhaRepository codigoResetSenhaRepository;
    private final UserServiceImpl userServiceImpl;
    private final EmailService emailService;
    private final AtletaService atletaService;
    private final ArenaService arenaService;

    private static final int RESEND_COOLDOWN_MINUTES = 2;
    private static final int MAX_RESENDS_PER_HOUR = 5;
    private static final int EXPIRATION_MINUTES = 15;

    @Transactional
    public void solicitarResetSenha(String email) {
        Usuario usuario = userServiceImpl.findUserByEmail(email);
        if (usuario == null) {
            // Por segurança, não revelamos se o email existe ou não
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Verifica limites de reenvio
        List<CodigoResetSenha> recentResends = codigoResetSenhaRepository
                .findByEmailAndLastResendAtAfter(email, now.minusHours(1));

        int totalResends = recentResends.stream().mapToInt(CodigoResetSenha::getResendCount).sum();
        if (totalResends >= MAX_RESENDS_PER_HOUR) {
            throw new ResendCodeLimitException("Limite de reenvios por hora excedido");
        }

        // Verifica cooldown
        List<CodigoResetSenha> validCodes = codigoResetSenhaRepository
                .findByEmailAndExpiresAtAfter(email, now);

        if (!validCodes.isEmpty()) {
            CodigoResetSenha latest = validCodes.getFirst();
            if (latest.getLastResendAt() != null &&
                    latest.getLastResendAt().plusMinutes(RESEND_COOLDOWN_MINUTES).isAfter(now)) {
                throw new InvalidCooldownResendConfirmationCodeException("Aguarde "
                        + RESEND_COOLDOWN_MINUTES + " minutos para reenviar");
            }
        }

        // Invalida códigos anteriores
        validCodes.forEach(code -> {
            code.setExpiresAt(now.minusSeconds(1));
            codigoResetSenhaRepository.save(code);
        });

        // Gera novo código
        CodigoResetSenha newCode = gerarCodigoResetSenha(email);
        newCode.setLastResendAt(now);
        newCode.setResendCount(validCodes.isEmpty() ? 1 : validCodes.getFirst().getResendCount() + 1);
        codigoResetSenhaRepository.save(newCode);

        emailService.enviarCodigoResetSenha(email, usuario.getNome(), newCode.getCode());
    }

    private CodigoResetSenha gerarCodigoResetSenha(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        return CodigoResetSenha.builder()
                .code(code)
                .email(email)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .resendCount(0)
                .build();
    }

    @Transactional
    public void verificarCodigoReset(String email, String code) {
        CodigoResetSenha codigo = codigoResetSenhaRepository.findByCodeAndEmail(code, email)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Código inválido"));

        if (codigo.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredConfirmationCodeException("Código expirado");
        }
    }

    @Transactional
    public void redefinirSenha(String email, String novaSenha) {
        Usuario usuario = userServiceImpl.findUserByEmail(email);
        if (usuario == null) {
            throw new UserNotFoundException("Usuário não encontrado");
        }
        if(usuario instanceof Atleta){
            atletaService.redefinirSenha(usuario, novaSenha);
        }
        if (usuario instanceof Arena){
            arenaService.redefinirSenha(usuario, novaSenha);
        }
    }
}
