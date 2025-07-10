package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.SlotHorario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByDataAgendamentoAndQuadra(LocalDate data, Quadra quadra);

    @Query("SELECT a FROM Agendamento a WHERE a.isPublico = true AND a.status = 'CONFIRMADO' " +
            "AND a.dataAgendamento >= :dataInicio")
    List<Agendamento> findAgendamentosPublicos(@Param("dataInicio") LocalDate dataInicio);

    Page<Agendamento> findByAtletaId(Long atletaId, Pageable pageable);

    // Métodos para agendamentos fixos
    List<Agendamento> findByAgendamentoFixoId(Long agendamentoFixoId);

    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.agendamentoFixo.id = :agendamentoFixoId " +
            "AND a.dataAgendamento >= :dataReferencia")
    long countAgendamentosFuturos(@Param("agendamentoFixoId") Long agendamentoFixoId,
                                  @Param("dataReferencia") LocalDate dataReferencia);

    // Verifica conflitos de agendamento em uma data específica
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a JOIN a.slotsHorario s " +
            "WHERE a.dataAgendamento = :data AND a.quadra.id = :quadraId " +
            "AND s.horarioInicio = :inicio AND s.horarioFim = :fim " +
            "AND a.status != 'CANCELADO'")
    boolean existeConflito(@Param("data") LocalDate data,
                           @Param("quadraId") Long quadraId,
                           @Param("inicio") LocalTime inicio,
                           @Param("fim") LocalTime fim);
}
