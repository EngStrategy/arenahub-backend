package com.engstrategy.alugai_api.repository.specs;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AgendamentoSpecs {

    public static Specification<Agendamento> hasAtletaId(UUID atletaId) {
        return (root, query, builder) ->
                builder.equal(root.get("atleta").get("id"), atletaId);
    }

    public static Specification<Agendamento> dataInicioAfterOrEqual(LocalDate dataInicio) {
        return (root, query, builder) -> {
            if (dataInicio == null) {
                return null;
            }
            return builder.greaterThanOrEqualTo(root.get("dataAgendamento"), dataInicio);
        };
    }

    public static Specification<Agendamento> dataFimBeforeOrEqual(LocalDate dataFim) {
        return (root, query, builder) -> {
            if (dataFim == null) {
                return null;
            }
            return builder.lessThanOrEqualTo(root.get("dataAgendamento"), dataFim);
        };
    }

    public static Specification<Agendamento> isTipoAgendamento(TipoAgendamento tipoAgendamento) {
        return (root, query, builder) -> {
            if (tipoAgendamento == null || tipoAgendamento == TipoAgendamento.AMBOS) {
                return null;
            }
            boolean isFixo = tipoAgendamento == TipoAgendamento.FIXO;
            return builder.equal(root.get("isFixo"), isFixo);
        };
    }

    // Novas Specifications para Jogos Abertos
    public static Specification<Agendamento> isPublico() {
        return (root, query, builder) -> builder.isTrue(root.get("isPublico"));
    }

    public static Specification<Agendamento> isPendente() {
        return (root, query, builder) -> builder.equal(root.get("status"), StatusAgendamento.PENDENTE);
    }

    public static Specification<Agendamento> hasVagas() {
        return (root, query, builder) -> builder.greaterThan(root.get("vagasDisponiveis"), 0);
    }

    public static Specification<Agendamento> hasCidade(String cidade) {
        return (root, query, builder) -> {
            if (cidade == null || cidade.trim().isEmpty()) {
                return null;
            }

            Join quadra = root.join("quadra");
            Join arena = quadra.join("arena");
            return builder.like(
                    builder.lower(arena.get("endereco").get("cidade")),
                    "%" + cidade.toLowerCase() + "%" // Usa LIKE para busca parcial
            );
        };
    }

    public static Specification<Agendamento> hasEsporte(String esporte) {
        return (root, query, builder) -> {
            if (esporte == null || esporte.trim().isEmpty()) {
                return null;
            }
            try {
                TipoEsporte tipoEsporte = TipoEsporte.valueOf(esporte.toUpperCase());
                return builder.equal(root.get("esporte"), tipoEsporte);
            } catch (IllegalArgumentException e) {
                return builder.disjunction();
            }
        };
    }

    public static Specification<Agendamento> isUpcoming() {
        return (root, query, builder) -> {
            ZoneId fusoHorarioBrasilia = ZoneId.of("America/Sao_Paulo");
            LocalDate hoje = LocalDate.now(fusoHorarioBrasilia);
            LocalTime agora = LocalTime.now(fusoHorarioBrasilia);

            // Condição 1: Agendamentos em datas futuras
            Predicate agendamentoEmDataFutura = builder.greaterThan(root.get("dataAgendamento"), hoje);

            // Condição 2: Agendamentos para hoje que ainda não começaram
            Predicate agendamentoHojeNaoIniciado = builder.and(
                    builder.equal(root.get("dataAgendamento"), hoje),
                    builder.greaterThan(root.get("horarioInicioSnapshot"), agora)
            );

            // A consulta final combina as duas condições com um "OU"
            return builder.or(agendamentoEmDataFutura, agendamentoHojeNaoIniciado);
        };
    }

    public static Specification<Agendamento> hasArenaId(UUID arenaId) {
        return (root, query, criteriaBuilder) -> {
            if (arenaId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Agendamento, Quadra> quadraJoin = root.join("quadra");
            Join<Quadra, Arena> arenaJoin = quadraJoin.join("arena");
            return criteriaBuilder.equal(arenaJoin.get("id"), arenaId);
        };
    }

    public static Specification<Agendamento> hasStatus(StatusAgendamento status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Agendamento> hasStatusIn(List<StatusAgendamento> statusList) {
        return (root, query, cb) -> {
            if (statusList == null || statusList.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("status").in(statusList);
        };
    }

    public static Specification<Agendamento> hasQuadraId(Long quadraId) {
        return (root, query, criteriaBuilder) -> {
            if (quadraId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("quadra").get("id"), quadraId);
        };
    }

    public static Specification<Agendamento> isAtivoEstrategico() {
        List<StatusAgendamento> statusAtivo = Arrays.asList(
                StatusAgendamento.PENDENTE,
                StatusAgendamento.AGUARDANDO_PAGAMENTO,
                StatusAgendamento.PAGO
        );
        return (root, query, cb) -> root.get("status").in(statusAtivo);
    }
}
