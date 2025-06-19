package com.engstrategy.alugai_api.repository.specs;

import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ArenaSpecs {

    public static Specification<Arena> hasCidade(String cidade) {
        return (root, query, criteriaBuilder) -> {
            if (cidade == null || cidade.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("endereco").get("cidade")),
                    "%" + cidade.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Arena> hasEsporte(String esporte) {
        return (root, query, criteriaBuilder) -> {
            if (esporte == null || esporte.trim().isEmpty()) {
                return null;
            }

            try {
                // Converter string para enum
                TipoEsporte tipoEsporte = TipoEsporte.valueOf(esporte.toUpperCase());

                // Fazer join com quadras e depois com tipoQuadra (ElementCollection)
                Join<Object, Object> quadrasJoin = root.join("quadras", JoinType.INNER);
                Join<Object, Object> esportesJoin = quadrasJoin.join("tipoQuadra", JoinType.INNER);

                // Adicionar distinct para evitar duplicatas
                query.distinct(true);

                return criteriaBuilder.equal(esportesJoin, tipoEsporte);

            } catch (IllegalArgumentException e) {
                // Se o esporte não for válido, retorna condição que sempre é falsa
                return criteriaBuilder.disjunction();
            }
        };
    }
}
