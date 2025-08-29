package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.CodigoVerificacaoSms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoVerificacaoSmsRepository extends JpaRepository<CodigoVerificacaoSms, Long> {

    Optional<CodigoVerificacaoSms> findByAtleta_TelefoneAndCodigo(String telefone, String codigo);

    Optional<CodigoVerificacaoSms> findByAtleta_Id(UUID atletaId);
}