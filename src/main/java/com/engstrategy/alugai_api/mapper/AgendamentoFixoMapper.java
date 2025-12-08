package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoFixoResponseDTO;
import com.engstrategy.alugai_api.dto.aula.InstrutorDetalhesDTO;
import com.engstrategy.alugai_api.model.AgendamentoFixo;
import com.engstrategy.alugai_api.model.Atleta;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoFixoMapper {

    public AgendamentoFixoResponseDTO fromAgendamentoFixoToResponseDTO(AgendamentoFixo aula) {
        if (aula == null) {
            return null;
        }

        InstrutorDetalhesDTO instrutorDTO = mapAtletaToInstrutorDTO(aula.getAtleta());

        return AgendamentoFixoResponseDTO.builder()
                .id(aula.getId())
                .nomeAula(aula.getNomeAula())
                .dataInicio(aula.getDataInicio())
                .dataFim(aula.getDataFim())
                .periodo(aula.getPeriodo())
                .status(aula.getStatus())
                .totalAgendamentos(aula.getAgendamentos() != null ? aula.getAgendamentos().size() : 0)
                .atletaId(aula.getAtleta().getId())
                .limiteAtletas(aula.getLimiteAtletas())
                .isElegivelWellhub(aula.isElegivelWellhub())
                .valorBaseMensal(aula.getValorBaseMensal())
                .valorPlanoTrimestral(aula.getValorPlanoTrimestral())
                .valorPlanoSemestral(aula.getValorPlanoSemestral())
                .instrutor(instrutorDTO)
                .agendamentos(null)
                .build();
    }

    private InstrutorDetalhesDTO mapAtletaToInstrutorDTO(Atleta atleta) {
        if (atleta == null) return null;

        return InstrutorDetalhesDTO.builder()
                .id(atleta.getId())
                .nome(atleta.getNome())
                .telefone(atleta.getTelefone())
                .urlFoto(atleta.getUrlFoto())
                .build();
    }
}