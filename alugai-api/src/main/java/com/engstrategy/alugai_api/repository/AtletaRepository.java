package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, Long> {

    Optional<Atleta> findAtletaByTelefone(String telefone);

    boolean existsByEmail(String email);

    boolean existsByTelefone(String telefone);

    Optional<Atleta> findByEmail(String email);

    // Para a Receita do Mês - Usando 'valorTotalSnapshot' e 'dataSnapshot'
    @Query("SELECT SUM(a.valorTotalSnapshot) FROM Agendamento a " +
            "WHERE a.quadra.arena.id = :arenaId " +
            "AND a.dataSnapshot BETWEEN :dataInicio AND :dataFim " +
            "AND a.status = 'CONFIRMADO'")
    BigDecimal calcularReceitaPorPeriodo(@Param("arenaId") Long arenaId,
                                         @Param("dataInicio") LocalDateTime dataInicio,
                                         @Param("dataFim") LocalDateTime dataFim);


    // Para Agendamentos Hoje
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.quadra.arena.id = :arenaId AND a.dataAgendamento = :dataAgendamento")
    int countByArenaIdAndDataAgendamento(@Param("arenaId") Long arenaId, @Param("dataAgendamento") LocalDate dataAgendamento);

    // Para Novos Clientes da Semana - Usando 'dataSnapshot'
    // Esta query conta atletas (clientes) únicos que fizeram seu primeiro agendamento na arena dentro do período.
    @Query("SELECT COUNT(DISTINCT a.atleta) FROM Agendamento a " +
            "WHERE a.quadra.arena.id = :arenaId AND a.dataSnapshot BETWEEN :dataInicio AND :dataFim")
    int countNovosClientesDaArenaPorPeriodo(@Param("arenaId") Long arenaId,
                                            @Param("dataInicio") LocalDateTime dataInicio,
                                            @Param("dataFim") LocalDateTime dataFim);

    // Para Próximos Agendamentos
    @Query("SELECT a FROM Agendamento a " +
            "WHERE a.quadra.arena.id = :arenaId " +
            "AND a.dataAgendamento = :dataAtual " +
            "AND a.horarioInicioSnapshot > :horarioAtual " + // Usando o campo de snapshot para consistência
            "ORDER BY a.horarioInicioSnapshot ASC " +
            "LIMIT 5")
    List<Agendamento> findProximosAgendamentosDoDia(@Param("arenaId") Long arenaId,
                                                    @Param("dataAtual") LocalDate dataAtual,
                                                    @Param("horarioAtual") LocalTime horarioAtual);

    @Query("SELECT a FROM Atleta a " +
            "WHERE lower(a.nome) LIKE lower(concat('%', :query, '%')) " +
            "OR ( " +
            "   :telefoneQuery <> '' AND " +
            "   REPLACE(REPLACE(REPLACE(REPLACE(a.telefone, '(', ''), ')', ''), '-', ''), ' ', '') LIKE concat('%', :telefoneQuery, '%')" +
            ")")
    List<Atleta> searchByNomeOuTelefone(
            @Param("query") String query,
            @Param("telefoneQuery") String telefoneQuery
    );
}
