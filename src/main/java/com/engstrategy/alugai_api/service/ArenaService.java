package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.agendamento.arena.CidadeDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaDashboardDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaResponseDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaUpdateDTO;
import com.engstrategy.alugai_api.dto.arena.CidadeResponseDTO;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArenaService {
    Arena criarArena(Arena arena);
    ArenaResponseDTO buscarPorId(Long id);
    Page<ArenaResponseDTO> listarTodos(Pageable pageable, String cidade, String esporte);
    Arena atualizar(Long id, ArenaUpdateDTO arenaUpdateDTO);
    void excluir(Long id);
    void redefinirSenha(Usuario usuario, String novaSenha);
    void alterarSenha(Long arenaId, String senhaAtual, String novaSenha);
    List<CidadeDTO> getCidades();
    ArenaDashboardDTO getDashboardData(Long arenaId);
}
