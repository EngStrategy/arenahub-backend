package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, UUID> {

    Optional<Atleta> findAtletaByTelefone(String telefone);

    boolean existsByEmail(String email);

    boolean existsByTelefone(String telefone);

    Optional<Atleta> findByEmail(String email);

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
