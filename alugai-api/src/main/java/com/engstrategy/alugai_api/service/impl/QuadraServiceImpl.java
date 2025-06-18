package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.quadra.QuadraUpdateDTO;
import com.engstrategy.alugai_api.exceptions.EntityNotFoundException;
import com.engstrategy.alugai_api.exceptions.QuadraArenaMismatchException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.QuadraRepository;
import com.engstrategy.alugai_api.service.QuadraService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuadraServiceImpl implements QuadraService {

    private final QuadraRepository quadraRepository;
    private final ArenaRepository arenaRepository;

    @Override
    @Transactional
    public Quadra criarQuadra(Quadra quadra, Long arenaId) {
        Arena arena = arenaRepository.findById(arenaId)
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada com ID: " + arenaId));
        quadra.setArena(arena);
        return quadraRepository.save(quadra);
    }

    @Override
    public Quadra buscarPorId(Long id) {
        return quadraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quadra não encontrada com ID: " + id));
    }

    @Override
    public Page<Quadra> listarTodos(Pageable pageable, Long arenaId, String esporte) {
        Specification<Quadra> spec = (root, query, builder) -> null;

        if (arenaId != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("arena").get("id"), arenaId));
        }

        if (esporte != null && !esporte.trim().isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.isMember(esporte, root.get("tipoQuadra")));
        }

        return quadraRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public Quadra atualizar(Long id, QuadraUpdateDTO quadraUpdateDTO) {
        Quadra savedQuadra = quadraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quadra não encontrada com ID: " + id));

        if (quadraUpdateDTO.getNomeQuadra() != null) {
            savedQuadra.setNomeQuadra(quadraUpdateDTO.getNomeQuadra());
        }
        if (quadraUpdateDTO.getUrlFotoQuadra() != null) {
            savedQuadra.setUrlFotoQuadra(quadraUpdateDTO.getUrlFotoQuadra());
        }
        if (quadraUpdateDTO.getTipoQuadra() != null) {
            savedQuadra.setTipoQuadra(quadraUpdateDTO.getTipoQuadra());
        }
        if (quadraUpdateDTO.getDescricao() != null) {
            savedQuadra.setDescricao(quadraUpdateDTO.getDescricao());
        }
        if (quadraUpdateDTO.getDuracaoReserva() != null) {
            savedQuadra.setDuracaoReserva(quadraUpdateDTO.getDuracaoReserva());
        }
        if (quadraUpdateDTO.getCobertura() != null) {
            savedQuadra.setCobertura(quadraUpdateDTO.getCobertura());
        }
        if (quadraUpdateDTO.getIluminacaoNoturna() != null) {
            savedQuadra.setIluminacaoNoturna(quadraUpdateDTO.getIluminacaoNoturna());
        }
        if (quadraUpdateDTO.getMateriaisFornecidos() != null) {
            savedQuadra.setMateriaisFornecidos(quadraUpdateDTO.getMateriaisFornecidos());
        }

        return quadraRepository.save(savedQuadra);
    }

    @Override
    @Transactional
    public void excluir(Long quadraId, Long arenaId) {
        Quadra quadra = quadraRepository.findById(quadraId)
                .orElseThrow(() -> new EntityNotFoundException("Quadra não encontrada com ID: " + quadraId));

        var arena = arenaRepository.findById(arenaId)
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada com ID: " + arenaId));

        if(!quadra.getArena().getId().equals(arena.getId())){
            throw new QuadraArenaMismatchException("A quadra com o id " + quadraId + " não pertence a arena com o id " +
                    arenaId);
        }

        quadraRepository.delete(quadra);
    }
}