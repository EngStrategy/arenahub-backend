package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
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
import java.util.Optional;
import java.util.UUID;

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
    BigDecimal calcularReceitaPorPeriodo(@Param("arenaId") UUID arenaId,
                                         @Param("dataInicio") LocalDateTime dataInicio,
                                         @Param("dataFim") LocalDateTime dataFim);


    // Para Agendamentos Hoje - A query está correta
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.quadra.arena.id = :arenaId AND a.dataAgendamento = :dataAgendamento")
    int countByArenaIdAndDataAgendamento(@Param("arenaId") UUID arenaId, @Param("dataAgendamento") LocalDate dataAgendamento);


    // Para Novos Clientes da Semana - Usando 'dataSnapshot'
    // Esta  conta atletas (clientes) únicos que fizeram seu primeiro agendamento na arena dentro do período.
    @Query("SELECT COUNT(DISTINCT a.atleta) FROM Agendamento a " +
            "WHERE a.quadra.arena.id = :arenaId AND a.dataSnapshot BETWEEN :dataInicio AND :dataFim")
    int countNovosClientesDaArenaPorPeriodo(@Param("arenaId") UUID arenaId,
                                            @Param("dataInicio") LocalDateTime dataInicio,
                                            @Param("dataFim") LocalDateTime dataFim);

    // Para Próximos Agendamentos - A query está correta
    @Query("SELECT a FROM Agendamento a " +
            "WHERE a.quadra.arena.id = :arenaId " +
            "AND a.dataAgendamento = :dataAtual " +
            "AND a.horarioInicioSnapshot > :horarioAtual " +
            "AND a.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'PAGO') " +
            "ORDER BY a.horarioInicioSnapshot ASC " +
            "LIMIT 5")
    List<Agendamento> findProximosAgendamentosDoDia(@Param("arenaId") UUID arenaId,
                                                    @Param("dataAtual") LocalDate dataAtual,
                                                    @Param("horarioAtual") LocalTime horarioAtual);

    /**
     * Busca agendamentos de um atleta que já terminaram,
     * não foram cancelados e ainda não possuem uma avaliação.
     */
    @Query("SELECT DISTINCT a FROM Agendamento a " +
            "JOIN FETCH a.slotsHorario sh " +
            "LEFT JOIN FETCH a.solicitacoes sol " +
            "LEFT JOIN FETCH a.avaliacao avaliacao " +
            "WHERE a.atleta.id = :atletaId " +
            "AND a.avaliacao IS NULL " +
            "AND (a.avaliacaoDispensada IS NULL OR a.avaliacaoDispensada = false) " +
            "AND a.status IN ('PAGO', 'CONFIRMADO') " +
            "AND ( (a.dataAgendamento < :dataAtual) OR " +
            "      (a.dataAgendamento = :dataAtual AND a.horarioFimSnapshot < :horaAtual) ) " +
            "ORDER BY a.dataAgendamento DESC, a.horarioFimSnapshot DESC")
    List<Agendamento> findAgendamentosPendentesDeAvaliacao(
            @Param("atletaId") UUID atletaId,
            @Param("dataAtual") LocalDate dataAtual,
            @Param("horaAtual") LocalTime horaAtual
    );

    /**
     * Busca todos os agendamentos com status PENDENTE para uma arena, a partir da data atual,
     * ordenados pela data e hora do agendamento.
     */
    @Query("SELECT a FROM Agendamento a " +
            "WHERE a.quadra.arena.id = :arenaId " +
            "AND a.status = 'PENDENTE' " +
            "AND ( a.dataAgendamento < :dataAtual OR " + // Critério 1: Agendamentos de dias anteriores
            "      (a.dataAgendamento = :dataAtual AND a.horarioInicioSnapshot <= :horaAtual) ) " + // Critério 2: Agendamentos de hoje cuja hora já passou
            "ORDER BY a.dataAgendamento ASC, a.horarioInicioSnapshot ASC")
    List<Agendamento> findPendentesAcaoByArenaId(
            @Param("arenaId") UUID arenaId,
            @Param("dataAtual") LocalDate dataAtual,
            @Param("horaAtual") LocalTime horaAtual
    );

    // --- MÉTODO PARA BUSCA DE JOGOS ABERTOS POR PROXIMIDADE ---
    @Query(
            value = "SELECT ag.*, (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(a.latitude)))) AS distance " +
                    "FROM agendamento ag " +
                    "JOIN quadra q ON ag.quadra_id = q.id " +
                    "JOIN arena a ON q.arena_id = a.id " +
                    "WHERE ag.is_publico = true " +
                    "AND ag.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'PAGO') " +
                    "AND ag.vagas_disponiveis > 0 " +
                    "AND ag.data_agendamento >= CURRENT_DATE " +
                    "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(a.latitude)))) < :raioKm " +
                    "ORDER BY distance ASC",
            countQuery = "SELECT count(ag.id) " +
                    "FROM agendamento ag " +
                    "JOIN quadra q ON ag.quadra_id = q.id " +
                    "JOIN arena a ON q.arena_id = a.id " +
                    "WHERE ag.is_publico = true " +
                    "AND ag.status IN ('PENDENTE', 'AGUARDANDO_PAGAMENTO', 'PAGO') " +
                    "AND ag.vagas_disponiveis > 0 " +
                    "AND ag.data_agendamento >= CURRENT_DATE " +
                    "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(a.latitude)))) < :raioKm",
            nativeQuery = true
    )
    Page<Agendamento> findJogosAbertosByProximity(@Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("raioKm") Double raioKm, Pageable pageable);

    @Query(value = "SELECT DISTINCT a FROM Agendamento a " +
            "LEFT JOIN FETCH a.atleta " +
            "LEFT JOIN FETCH a.quadra " +
            "LEFT JOIN FETCH a.slotsHorario " +
            "LEFT JOIN FETCH a.participantes " +
            "LEFT JOIN FETCH a.avaliacao avaliacao " +
            "WHERE a.quadra.arena.id = :arenaId " +

            // CORREÇÃO: Usar COALESCE para filtros de data opcionais na query principal
            "AND a.dataAgendamento >= COALESCE(:dataInicio, a.dataAgendamento) " +
            "AND a.dataAgendamento <= COALESCE(:dataFim, a.dataAgendamento) " +

            "AND (:quadraId IS NULL OR a.quadra.id = :quadraId) " +
            "AND (:statuses IS NULL OR a.status IN :statuses)",

            // Count Query também deve usar COALESCE
            countQuery = "SELECT COUNT(DISTINCT a) FROM Agendamento a " +
                    "WHERE a.quadra.arena.id = :arenaId " +

                    // CORREÇÃO NO COUNT QUERY
                    "AND a.dataAgendamento >= COALESCE(:dataInicio, a.dataAgendamento) " +
                    "AND a.dataAgendamento <= COALESCE(:dataFim, a.dataAgendamento) " +

                    "AND (:quadraId IS NULL OR a.quadra.id = :quadraId) " +
                    "AND (:statuses IS NULL OR a.status IN :statuses)")
    Page<Agendamento> findByArenaIdWithFilters(
            @Param("arenaId") UUID arenaId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("quadraId") Long quadraId,
            @Param("statuses") List<StatusAgendamento> statuses,
            Pageable pageable
    );

    @Query("SELECT COUNT(a) FROM Agendamento a " +
            "WHERE a.quadra.id = :quadraId " +
            "AND a.dataAgendamento >= :dataAtual " +
            "AND a.status NOT IN ('CANCELADO', 'FINALIZADO', 'AUSENTE')")
    long countAgendamentosPendentesPorQuadra(
            @Param("quadraId") Long quadraId,
            @Param("dataAtual") LocalDate dataAtual
    );

    @Query("SELECT a FROM Agendamento a WHERE a.status = :status AND a.dataSnapshot < :limite")
    List<Agendamento> findExpirados(@Param("status") StatusAgendamento status, @Param("limite") LocalDateTime limite);

    @Query("SELECT a FROM Agendamento a " +
            "JOIN FETCH a.atleta atleta " +
            "JOIN FETCH a.quadra quadra " +
            "JOIN FETCH quadra.arena arena " +
            "WHERE a.asaasPaymentId = :asaasPaymentId")
    Optional<Agendamento> findByAsaasPaymentIdFetchRelations(@Param("asaasPaymentId") String asaasPaymentId);

    /**
     * Busca agendamentos de um atleta com os slotsHorario carregados (Eagerly).
     */
    @Query(value = "SELECT a FROM Agendamento a " +
            "JOIN FETCH a.slotsHorario sh " +
            "LEFT JOIN FETCH a.solicitacoes sol " +
            "LEFT JOIN FETCH a.participantes p " +
            "WHERE a.atleta.id = :atletaId " +
            "AND a.dataAgendamento >= COALESCE(:dataInicio, a.dataAgendamento) " +
            "AND a.dataAgendamento <= COALESCE(:dataFim, a.dataAgendamento) " +
            "AND (:isFixoFiltro IS NULL OR a.isFixo = :isFixoFiltro) " +
            "AND a.status IN :statusFilter",
            countQuery = "SELECT COUNT(a) FROM Agendamento a " +
                    "WHERE a.atleta.id = :atletaId " +
                    "AND a.dataAgendamento >= COALESCE(:dataInicio, a.dataAgendamento) " +
                    "AND a.dataAgendamento <= COALESCE(:dataFim, a.dataAgendamento) " +
                    "AND (:isFixoFiltro IS NULL OR a.isFixo = :isFixoFiltro) " +
                    "AND a.status IN :statusFilter"
    )
    Page<Agendamento> findByAtletaIdWithDetails(
            @Param("atletaId") UUID atletaId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("isFixoFiltro") Boolean isFixoFiltro,
            @Param("statusFilter") List<StatusAgendamento> statusFilter,
            Pageable pageable
    );

}
