package com.engstrategy.alugai_api.repository.specs;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.enums.TipoAgendamento;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AgendamentoSpecs {

    public static Specification<Agendamento> hasAtletaId(Long atletaId) {
        return (root, query, builder) -> builder.equal(root.get("atleta").get("id"), atletaId);
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
}
