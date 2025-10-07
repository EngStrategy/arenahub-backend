package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuadraRepository extends JpaRepository<Quadra, Long>, JpaSpecificationExecutor<Quadra> {
    boolean existsByNomeQuadraIgnoreCase(String nome);

    Long countByArenaId(UUID arenaId);

    @Query("SELECT DISTINCT q FROM Quadra q " +
            "LEFT JOIN FETCH q.horariosFuncionamento hf " +
            "LEFT JOIN FETCH hf.intervalosDeHorario " +
            "LEFT JOIN FETCH q.tipoQuadra " +
            "LEFT JOIN FETCH q.materiaisFornecidos " +
            "WHERE q.arena.id = :arenaId")
    List<Quadra> findByArenaIdWithDetails(@Param("arenaId") UUID arenaId);

    @Query("SELECT DISTINCT q FROM Quadra q " +
            "LEFT JOIN FETCH q.horariosFuncionamento hf " +
            "LEFT JOIN FETCH hf.intervalosDeHorario ih " +
            "LEFT JOIN FETCH ih.slotsHorario " +
            "LEFT JOIN FETCH q.tipoQuadra " +
            "LEFT JOIN FETCH q.materiaisFornecidos " +
            "WHERE q.id = :quadraId")
    Optional<Quadra> findByIdWithDetails(@Param("quadraId") Long quadraId);

    @Query(value = "SELECT DISTINCT q FROM Quadra q " +
            "LEFT JOIN FETCH q.horariosFuncionamento hf " +
            "LEFT JOIN FETCH hf.intervalosDeHorario " +
            "LEFT JOIN FETCH q.tipoQuadra " +
            "LEFT JOIN FETCH q.materiaisFornecidos " +
            "WHERE (:arenaId is null OR q.arena.id = :arenaId) " +
            "AND (:esporte is null OR :esporte MEMBER OF q.tipoQuadra)",
            countQuery = "SELECT COUNT(DISTINCT q) FROM Quadra q " +
                    "WHERE (:arenaId is null OR q.arena.id = :arenaId) " +
                    "AND (:esporte is null OR :esporte MEMBER OF q.tipoQuadra)")
    Page<Quadra> findQuadrasByFiltersWithDetails(
            @Param("arenaId") UUID arenaId,
            @Param("esporte") TipoEsporte esporte,
            Pageable pageable);
}
