package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.model.enums.Role;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import com.engstrategy.alugai_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ArenaRepository arenaRepository;
    private final AtletaRepository atletaRepository;

    public boolean existsByEmail(String email) {
        return arenaRepository.existsByEmail(email) | atletaRepository.existsByEmail(email);
    }

    public boolean existsByTelefone(String telefone) {
        return arenaRepository.existsByTelefone(telefone) | atletaRepository.existsByTelefone(telefone);
    }

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

    public Usuario findUserById(UUID id, Role role) {
        if (role == Role.ARENA) {
            return arenaRepository.findById(id).orElse(null);
        } else if (role == Role.ATLETA) {
            return atletaRepository.findById(id).orElse(null);
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = findUserByEmail(email);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email);
        }
        return new CustomUserDetails(usuario);
    }
}
