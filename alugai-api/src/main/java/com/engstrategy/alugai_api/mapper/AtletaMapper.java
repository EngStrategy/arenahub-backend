package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.atleta.AtletaCreateDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaResponseDTO;
import com.engstrategy.alugai_api.model.Atleta;
import org.springframework.stereotype.Component;

@Component
public class AtletaMapper {

    public Atleta mapAtletaCreateDtoToAtleta(AtletaCreateDTO atletaCreateDTO) {
        return Atleta.builder()
                .nome(atletaCreateDTO.getNome())
                .email(atletaCreateDTO.getEmail())
                .telefone(atletaCreateDTO.getTelefone())
                .senha(atletaCreateDTO.getSenha())
                .urlFoto(atletaCreateDTO.getUrlFoto())
                .role(atletaCreateDTO.getRole())
                .build();
    }

    public AtletaResponseDTO mapAtletaToAtletaResponseDto(Atleta atleta) {
        return AtletaResponseDTO.builder()
                .id(atleta.getId())
                .nome(atleta.getNome())
                .email(atleta.getEmail())
                .telefone(atleta.getTelefone())
                .dataCriacao(atleta.getDataCriacao())
                .urlFoto(atleta.getUrlFoto())
                .role(atleta.getRole())
                .build();
    }
}
