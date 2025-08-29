package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.AgendamentoFixo;
import com.engstrategy.alugai_api.model.enums.StatusAgendamentoFixo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgendamentoFixoRepository extends JpaRepository<AgendamentoFixo, Long> {
    List<AgendamentoFixo> findByAtletaIdAndStatus(UUID atletaId, StatusAgendamentoFixo status);
}
