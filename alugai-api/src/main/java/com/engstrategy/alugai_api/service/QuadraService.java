package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.quadra.QuadraUpdateDTO;
import com.engstrategy.alugai_api.model.Quadra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface QuadraService {
    Quadra criarQuadra(Quadra quadra, Long arenaId);
    Quadra buscarPorId(Long id);
    Page<Quadra> listarTodos(Pageable pageable, Long arenaId, String esporte);
    Quadra atualizar(Long id, QuadraUpdateDTO quadraUpdateDTO, Long arenaId);
    void excluir(Long id, Long arenaId);
    List<Quadra> buscarPorArenaId(Long arenaId);
}
