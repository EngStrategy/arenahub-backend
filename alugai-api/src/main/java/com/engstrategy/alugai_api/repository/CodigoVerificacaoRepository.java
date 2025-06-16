package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.CodigoVerificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CodigoVerificacaoRepository extends JpaRepository<CodigoVerificacao, Long> {
    Optional<CodigoVerificacao> findByCodeAndEmail(String code, String email);
    List<CodigoVerificacao> findByEmailAndExpiresAtAfter(String email, LocalDateTime now);
    List<CodigoVerificacao> findByEmailAndLastResendAtAfter(String email, LocalDateTime after);
}
