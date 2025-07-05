package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.AgendamentoFixo;
import com.engstrategy.alugai_api.model.enums.StatusAgendamentoFixo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AgendamentoFixoRepository extends JpaRepository<AgendamentoFixo, Long> {

    List<AgendamentoFixo> findByAtletaIdAndStatus(Long atletaId, StatusAgendamentoFixo status);

    @Query("SELECT af FROM AgendamentoFixo af WHERE af.atleta.id = :atletaId " +
            "AND af.status = :status ORDER BY af.dataCriacao DESC")
    Page<AgendamentoFixo> findByAtletaIdAndStatus(@Param("atletaId") Long atletaId,
                                                  @Param("status") StatusAgendamentoFixo status,
                                                  Pageable pageable);
}
