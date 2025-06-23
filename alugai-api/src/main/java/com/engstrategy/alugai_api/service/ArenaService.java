package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.arena.ArenaUpdateDTO;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArenaService {
    Arena criarArena(Arena arena);
    Arena buscarPorId(Long id);
    Page<Arena> listarTodos(Pageable pageable, String cidade, String esporte);
    Arena atualizar(Long id, ArenaUpdateDTO arenaUpdateDTO);
    void excluir(Long id);
    void redefinirSenha(Usuario usuario, String novaSenha);
}
