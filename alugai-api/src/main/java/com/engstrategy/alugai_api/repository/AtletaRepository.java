package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, Long> {

    boolean existsByEmail(String email);

    boolean existsByTelefone(String telefone);

    Optional<Atleta> findByEmail(String email);
}
