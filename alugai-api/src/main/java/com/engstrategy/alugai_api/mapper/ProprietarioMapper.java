package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.proprietario.ProprietarioCadastroDTO;
import com.engstrategy.alugai_api.dto.proprietario.ProprietarioResponseDTO;
import org.mapstruct.*;

/**
 * Mapper responsável por conversões entre Proprietario e seus respectivos DTOs.
 */
@Mapper(componentModel = "spring")
public interface ProprietarioMapper {

    /**
     * Converte um DTO de cadastro em uma entidade Proprietario.
     *
     * @param dto objeto com dados de entrada
     * @return entidade Proprietario pronta para persistência
     */
    Proprietario toEntity(ProprietarioCadastroDTO dto);

    /**
     * Converte a entidade Proprietario para um DTO de resposta.
     *
     * @param proprietario entidade persistida
     * @return DTO com dados públicos do proprietário
     */
    ProprietarioResponseDTO toDto(Proprietario proprietario);
}

