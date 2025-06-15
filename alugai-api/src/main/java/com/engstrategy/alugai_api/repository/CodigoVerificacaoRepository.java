package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.CodigoVerificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoVerificacaoRepository extends JpaRepository<CodigoVerificacao, Long> {
    Optional<CodigoVerificacao> findByCodeAndEmail(String code, String email);
}
