package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Arena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArenaRepository extends JpaRepository<Arena, Long>, JpaSpecificationExecutor<Arena> {

    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);
    boolean existsByCpfProprietario(String cpfProprietario);
    boolean existsByCnpj(String cnpj);
    Optional<Arena> findByEmail(String email);

    @Query("SELECT DISTINCT a.endereco.cidade, a.endereco.estado FROM Arena a")
    List<Object[]> findDistinctCidadeAndEstado();
}