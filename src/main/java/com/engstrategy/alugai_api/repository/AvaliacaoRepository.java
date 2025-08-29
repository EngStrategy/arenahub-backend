package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.dto.arena.ArenaRatingInfo;
import com.engstrategy.alugai_api.dto.quadra.QuadraRatingInfo;
import com.engstrategy.alugai_api.model.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    // Query para buscar todas as avaliações de uma quadra específica
    Page<Avaliacao> findByAgendamento_Quadra_IdOrderByDataAvaliacaoDesc(Long quadraId, Pageable pageable);

    // Query para buscar a média e contagem de avaliações para UMA arena
    @Query("SELECT new com.engstrategy.alugai_api.dto.arena.ArenaRatingInfo(AVG(av.nota), COUNT(av.id)) " +
            "FROM Avaliacao av " +
            "WHERE av.agendamento.quadra.arena.id = :arenaId")
    ArenaRatingInfo findArenaRatingInfoByArenaId(@Param("arenaId") UUID arenaId);

    // Query para buscar a média e contagem para VÁRIAS arenas de uma só vez
    @Query("SELECT av.agendamento.quadra.arena.id as arenaId, AVG(av.nota) as notaMedia, COUNT(av.id) as quantidadeAvaliacoes " +
            "FROM Avaliacao av " +
            "WHERE av.agendamento.quadra.arena.id IN :arenaIds " +
            "GROUP BY av.agendamento.quadra.arena.id")
    List<Map<String, Object>> findArenaRatingInfoForArenas(@Param("arenaIds") List<UUID> arenaIds);

    // Query para buscar a média e contagem de avaliações para UMA quadra
    @Query("SELECT new com.engstrategy.alugai_api.dto.quadra.QuadraRatingInfo(av.agendamento.quadra.id, AVG(av.nota), COUNT(av.id)) " +
            "FROM Avaliacao av " +
            "WHERE av.agendamento.quadra.id IN :quadraIds " +
            "GROUP BY av.agendamento.quadra.id")
    List<QuadraRatingInfo> findQuadraRatingInfoForQuadras(@Param("quadraIds") List<Long> quadraIds);

}