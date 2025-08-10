package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.SolicitacaoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoEntradaRepository extends JpaRepository<SolicitacaoEntrada, Long> {
    Optional<SolicitacaoEntrada> findByAgendamentoIdAndSolicitanteId(Long agendamentoId, Long solicitanteId);
    List<SolicitacaoEntrada> findByAgendamentoId(Long agendamentoId);
    List<SolicitacaoEntrada> findBySolicitanteIdOrderByAgendamentoDataAgendamentoDesc(Long solicitanteId);
}
