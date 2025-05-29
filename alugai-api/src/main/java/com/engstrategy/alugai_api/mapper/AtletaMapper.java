package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.atleta.AtletaCadastroDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaResponseDTO;
import com.engstrategy.alugai_api.model.Atleta;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

/**
 * Mapper responsável por conversões entre Atleta e seus respectivos DTOs.
 * Utiliza MapStruct para gerar a implementação automaticamente.
 */
@Mapper(componentModel = "spring")
public interface AtletaMapper {

    /**
     * Converte um DTO de cadastro em uma entidade Atleta.
     * Usado durante o processo de criação.
     */
    Atleta toEntity(AtletaCadastroDTO dto);

    /**
     * Converte a entidade Atleta para um DTO de resposta.
     * Usado para retorno ao cliente (sem senha, por exemplo).
     */
    AtletaResponseDTO toDto(Atleta atleta);

}

