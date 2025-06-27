package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.CodigoResetSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CodigoResetSenhaRepository extends JpaRepository<CodigoResetSenha, Long> {
    Optional<CodigoResetSenha> findByCodeAndEmail(String code, String email);
    List<CodigoResetSenha> findByEmailAndExpiresAtAfter(String email, LocalDateTime now);
    List<CodigoResetSenha> findByEmailAndLastResendAtAfter(String email, LocalDateTime after);
}
