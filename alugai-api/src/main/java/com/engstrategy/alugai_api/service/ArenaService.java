package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.arena.ArenaCreateDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaResponseDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArenaService {
    ArenaResponseDTO criarArena(ArenaCreateDTO arenaCreateDTO);
    ArenaResponseDTO buscarPorId(Long id);
    Page<ArenaResponseDTO> listarTodos(Pageable pageable, String cidade, String esporte);
    ArenaResponseDTO atualizar(Long id, ArenaUpdateDTO arenaUpdateDTO);
    void excluir(Long id);
}
