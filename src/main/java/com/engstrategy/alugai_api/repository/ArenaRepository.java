package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Arena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArenaRepository extends JpaRepository<Arena, UUID>, JpaSpecificationExecutor<Arena> {

    boolean existsByEmail(String email);

    boolean existsByTelefone(String telefone);

    boolean existsByCpfProprietario(String cpfProprietario);

    boolean existsByCnpj(String cnpj);

    Optional<Arena> findByEmail(String email);

    @Query("SELECT DISTINCT a.endereco.cidade, a.endereco.estado FROM Arena a")
    List<Object[]> findDistinctCidadeAndEstado();

    /**
     * Busca uma Arena por ID e carrega (EAGER) todas as suas quadras,
     * os horários de funcionamento de cada quadra e os intervalos de cada horário.
     */
    @Query("SELECT a FROM Arena a " +
            "LEFT JOIN FETCH a.quadras q " +
            "LEFT JOIN FETCH q.horariosFuncionamento hf " +
            "LEFT JOIN FETCH hf.intervalosDeHorario " +
            "WHERE a.id = :arenaId")
    Optional<Arena> findByIdFetchingQuadrasAndHorarios(@Param("arenaId") UUID arenaId);

    @Query(
            value = "SELECT *, (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(a.latitude)))) AS distance " +
                    "FROM arena a " +
                    "WHERE a.ativo = true AND (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(a.latitude)))) < :raioKm " +
                    "ORDER BY distance ASC",
            countQuery = "SELECT count(*) FROM arena a " +
                    "WHERE a.ativo = true AND (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(a.latitude)))) < :raioKm",
            nativeQuery = true
    )
    Page<Arena> findByProximity(@Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("raioKm") Double raioKm, Pageable pageable);
}
