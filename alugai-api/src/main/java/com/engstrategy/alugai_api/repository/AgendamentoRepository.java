package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.SlotHorario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento> {

    List<Agendamento> findByDataAgendamentoAndQuadra(LocalDate data, Quadra quadra);

    @Query("SELECT a FROM Agendamento a WHERE a.isPublico = true AND a.status = 'PENDENTE' " +
            "AND a.vagasDisponiveis > 0 AND a.dataAgendamento >= :dataInicio")
    List<Agendamento> findAgendamentosPublicos(@Param("dataInicio") LocalDate dataInicio);

    // Métodos para agendamentos fixos
    List<Agendamento> findByAgendamentoFixoId(Long agendamentoFixoId);

    // Verifica conflitos de agendamento em uma data específica
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a JOIN a.slotsHorario s " +
            "WHERE a.dataAgendamento = :data AND a.quadra.id = :quadraId " +
            "AND s.horarioInicio = :inicio AND s.horarioFim = :fim " +
            "AND a.status != 'CANCELADO'")
    boolean existeConflito(@Param("data") LocalDate data,
                           @Param("quadraId") Long quadraId,
                           @Param("inicio") LocalTime inicio,
                           @Param("fim") LocalTime fim);


    // Para a Receita do Mês - Usando 'valorTotalSnapshot' e 'dataSnapshot'
    @Query("SELECT SUM(a.valorTotalSnapshot) FROM Agendamento a " +
            "WHERE a.quadra.arena.id = :arenaId " +
            "AND a.dataSnapshot BETWEEN :dataInicio AND :dataFim " +
            "AND a.status = 'PAGO'")
    BigDecimal calcularReceitaPorPeriodo(@Param("arenaId") Long arenaId,
                                         @Param("dataInicio") LocalDateTime dataInicio,
                                         @Param("dataFim") LocalDateTime dataFim);


    // Para Agendamentos Hoje - A query está correta
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.quadra.arena.id = :arenaId AND a.dataAgendamento = :dataAgendamento")
    int countByArenaIdAndDataAgendamento(@Param("arenaId") Long arenaId, @Param("dataAgendamento") LocalDate dataAgendamento);


    // Para Novos Clientes da Semana - Usando 'dataSnapshot'
    // Esta  conta atletas (clientes) únicos que fizeram seu primeiro agendamento na arena dentro do período.
    @Query("SELECT COUNT(DISTINCT a.atleta) FROM Agendamento a " +
            "WHERE a.quadra.arena.id = :arenaId AND a.dataSnapshot BETWEEN :dataInicio AND :dataFim")
    int countNovosClientesDaArenaPorPeriodo(@Param("arenaId") Long arenaId,
                                            @Param("dataInicio") LocalDateTime dataInicio,
                                            @Param("dataFim") LocalDateTime dataFim);

    // Para Próximos Agendamentos - A query está correta
    @Query("SELECT a FROM Agendamento a " +
            "WHERE a.quadra.arena.id = :arenaId " +
            "AND a.dataAgendamento = :dataAtual " +
            "AND a.horarioInicioSnapshot > :horarioAtual " + // Usando o campo de snapshot para consistência
            "ORDER BY a.horarioInicioSnapshot ASC " +
            "LIMIT 5")
    List<Agendamento> findProximosAgendamentosDoDia(@Param("arenaId") Long arenaId,
                                                    @Param("dataAtual") LocalDate dataAtual,
                                                    @Param("horarioAtual") LocalTime horarioAtual);
}
