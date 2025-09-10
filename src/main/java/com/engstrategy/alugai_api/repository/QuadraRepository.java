package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Quadra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface QuadraRepository extends JpaRepository<Quadra, Long>, JpaSpecificationExecutor<Quadra> {
    boolean existsByNomeQuadraIgnoreCase(String nome);
    List<Quadra> findByArenaId(UUID arenaId);

    Long countByArenaId(UUID arenaId);
}
