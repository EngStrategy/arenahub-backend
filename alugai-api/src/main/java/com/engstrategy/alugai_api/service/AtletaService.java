package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.atleta.AtletaCreateDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaResponseDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AtletaService {
    AtletaResponseDTO criarAtleta(AtletaCreateDTO atletaCreateDTO);
    AtletaResponseDTO buscarPorId(Long id);
    Page<AtletaResponseDTO> listarTodos(Pageable pageable);
    AtletaResponseDTO atualizar(Long id, AtletaUpdateDTO atletaUpdateDTO);
    void excluir(Long id);
}
