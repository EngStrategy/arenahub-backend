package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Quadra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface QuadraRepository extends JpaRepository<Quadra, Long>, JpaSpecificationExecutor<Quadra> {
    boolean existsByNomeQuadra(String nome);
    List<Quadra> findByArenaId(Long arenaId);
}
