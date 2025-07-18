package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.cidades.CidadesResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CidadesMapper {

    public CidadesResponseDTO toCidadesResponseDTO(String cidade) {
        return CidadesResponseDTO.builder()
                .cidade(cidade)
                .build();
    }
}
