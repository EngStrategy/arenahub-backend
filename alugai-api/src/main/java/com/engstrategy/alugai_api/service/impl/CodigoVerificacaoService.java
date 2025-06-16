package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.exceptions.AlreadyConfirmedEmailException;
import com.engstrategy.alugai_api.exceptions.InvalidCooldownResendConfirmationCodeException;
import com.engstrategy.alugai_api.exceptions.ResendCodeLimitException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.CodigoVerificacao;
import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import com.engstrategy.alugai_api.repository.CodigoVerificacaoRepository;
import com.engstrategy.alugai_api.util.GeradorCodigoVerificacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CodigoVerificacaoService {
    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
    private final AtletaRepository atletaRepository;
    private final ArenaRepository arenaRepository;
    private final UserService userService;
    private final EmailService emailService;

    private static final int RESEND_COOLDOWN_MINUTES = 2;
    private static final int MAX_RESENDS_PER_HOUR = 5;

    public Optional<CodigoVerificacao> getCode(String code, String email) {
        return codigoVerificacaoRepository.findByCodeAndEmail(code, email);
    }

    @Transactional
    public void confirmCode(CodigoVerificacao codigo) {
        Usuario usuario = userService.findUserByEmail(codigo.getEmail());

        if (usuario == null) {
            throw new UserNotFoundException("Usuário não encontrado");
        }
        if (usuario.isAtivo()) {
            throw new AlreadyConfirmedEmailException("Email já confirmado");
        }

        codigo.setConfirmedAt(LocalDateTime.now());
        usuario.setAtivo(true);

        if(usuario instanceof Atleta) {
            atletaRepository.save((Atleta) usuario);
        }

        if(usuario instanceof Arena) {
            arenaRepository.save((Arena) usuario);
        }

        codigoVerificacaoRepository.save(codigo);
    }

    @Transactional
    public void resendVerificationCode(String email) {
        Usuario usuario = userService.findUserByEmail(email);

        if (usuario == null) {
            throw new UserNotFoundException("Email não registrado");
        }
        if (usuario.isAtivo()) {
            throw new AlreadyConfirmedEmailException("Email já confirmado");
        }

        LocalDateTime now = LocalDateTime.now();

        // Verifica limite de reenvios por hora
        List<CodigoVerificacao> recentResends = codigoVerificacaoRepository
                .findByEmailAndLastResendAtAfter(email, now.minusHours(1));
        int totalResends = recentResends.stream().mapToInt(CodigoVerificacao::getResendCount).sum();
        if (totalResends >= MAX_RESENDS_PER_HOUR) {
            throw new ResendCodeLimitException("Limite de reenvios por hora excedido");
        }

        // Verifica cooldown
        List<CodigoVerificacao> validCodes = codigoVerificacaoRepository
                .findByEmailAndExpiresAtAfter(email, now);
        if (!validCodes.isEmpty()) {
            CodigoVerificacao latest = validCodes.getFirst();
            if (latest.getLastResendAt() != null &&
                    latest.getLastResendAt().plusMinutes(RESEND_COOLDOWN_MINUTES).isAfter(now)) {
                throw new InvalidCooldownResendConfirmationCodeException("Aguarde " + RESEND_COOLDOWN_MINUTES + " minutos para reenviar");
            }
        }

        // Invalida códigos anteriores
        validCodes.forEach(code -> {
            code.setExpiresAt(now.minusSeconds(1));
            codigoVerificacaoRepository.save(code);
        });

        // Gera e envia novo código
        CodigoVerificacao newCode = GeradorCodigoVerificacao.gerarCodigoVerificacao(email);
        newCode.setLastResendAt(now);
        newCode.setResendCount(validCodes.isEmpty() ? 1 : validCodes.getFirst().getResendCount() + 1);
        codigoVerificacaoRepository.save(newCode);

        emailService.enviarCodigoVerificacao(email, userService.findUserByEmail(email).getNome(), newCode.getCode());
    }
}
