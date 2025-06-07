package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.atleta.AtletaUpdateDTO;
import com.engstrategy.alugai_api.model.Atleta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AtletaService {
    Atleta criarAtleta(Atleta atleta);
    Atleta buscarPorId(Long id);
    Page<Atleta> listarTodos(Pageable pageable);
    Atleta atualizar(Long id, AtletaUpdateDTO atletaUpdateDTO);
    void excluir(Long id);
}
