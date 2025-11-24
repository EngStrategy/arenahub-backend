package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.quadra.QuadraResponseDTO;
import com.engstrategy.alugai_api.dto.quadra.QuadraUpdateDTO;
import com.engstrategy.alugai_api.dto.quadra.SlotHorarioResponseDTO;
import com.engstrategy.alugai_api.model.Quadra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface QuadraService {
    Quadra criarQuadra(Quadra quadra, UUID arenaId);
    Quadra buscarPorId(Long id);
    Page<Quadra> listarTodos(Pageable pageable, UUID arenaId, String esporte);
    QuadraResponseDTO atualizar(Long id, QuadraUpdateDTO quadraUpdateDTO, UUID arenaId);
    void excluir(Long id, UUID arenaId);
    List<QuadraResponseDTO> buscarPorArenaId(UUID arenaId);
    List<SlotHorarioResponseDTO> consultarDisponibilidade(Long quadraId, LocalDate data);
}
