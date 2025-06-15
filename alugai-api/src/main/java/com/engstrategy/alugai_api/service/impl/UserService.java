package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.model.enums.Role;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ArenaRepository arenaRepository;
    private final AtletaRepository atletaRepository;

    public Usuario findUserByEmail(String email) {
        // Primeiro tenta buscar na tabela Arena
        Optional<Arena> arena = arenaRepository.findByEmail(email);
        if (arena.isPresent()) {
            return arena.get();
        }

        // Se não encontrar, busca na tabela Atleta
        Optional<Atleta> atleta = atletaRepository.findByEmail(email);
        return atleta.orElse(null);
    }

    public Usuario findUserById(Long id, Role role) {
        if (role == Role.ARENA) {
            return arenaRepository.findById(id).orElse(null);
        } else if (role == Role.ATLETA) {
            return atletaRepository.findById(id).orElse(null);
        }
        return null;
    }
}
