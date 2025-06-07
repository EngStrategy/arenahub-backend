package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.arena.ArenaUpdateDTO;
import com.engstrategy.alugai_api.mapper.EnderecoMapper;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.specs.ArenaSpecs;
import com.engstrategy.alugai_api.service.ArenaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArenaServiceImpl implements ArenaService {

    private final ArenaRepository arenaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnderecoMapper enderecoMapper;

    @Override
    @Transactional
    public Arena criarArena(Arena arena) {
        validarDadosUnicos(arena.getEmail(), arena.getTelefone(),
                arena.getCpfProprietario(), arena.getCnpj());

        encodePassword(arena);
        return arenaRepository.save(arena);
    }

    @Override
    public Arena buscarPorId(Long id) {
        return arenaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada com ID: " + id));
    }

    @Override
    public Page<Arena> listarTodos(Pageable pageable, String cidade, String esporte) {
        // Criar Specification base
        Specification<Arena> spec = (root, query, builder) -> null;

        // Adicionar filtro de cidade se fornecido
        if (cidade != null && !cidade.trim().isEmpty()) {
            spec = spec.and(ArenaSpecs.hasCidade(cidade));
        }

        // Adicionar filtro de esporte se fornecido
        if (esporte != null && !esporte.trim().isEmpty()) {
            spec = spec.and(ArenaSpecs.hasEsporte(esporte));
        }

        // Executar consulta com specifications
        return arenaRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public Arena atualizar(Long id, ArenaUpdateDTO arenaUpdateDTO) {
        Arena savedArena = arenaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada com ID: " + id));

        if (arenaUpdateDTO.getTelefone() != null && !arenaUpdateDTO.getTelefone().equals(savedArena.getTelefone())) {
            if (arenaRepository.existsByTelefone(arenaUpdateDTO.getTelefone())) {
                throw new IllegalArgumentException("Telefone já está em uso.");
            }
            savedArena.setTelefone(arenaUpdateDTO.getTelefone());
        }

        if (arenaUpdateDTO.getNome() != null) {
            savedArena.setNome(arenaUpdateDTO.getNome());
        }
        if (arenaUpdateDTO.getDescricao() != null) {
            savedArena.setDescricao(arenaUpdateDTO.getDescricao());
        }
        if (arenaUpdateDTO.getUrlFoto() != null) {
            savedArena.setUrlFoto(arenaUpdateDTO.getUrlFoto());
        }
        if (arenaUpdateDTO.getEndereco() != null) {
            savedArena.setEndereco(enderecoMapper.mapEnderecoDtoToEndereco(arenaUpdateDTO.getEndereco()));
        }

        return arenaRepository.save(savedArena);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Arena arena = arenaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada com ID: " + id));
        arenaRepository.delete(arena);
    }

    private void validarDadosUnicos(String email, String telefone, String cpfProprietario, String cnpj) {
        if (arenaRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já está em uso.");
        }
        if (arenaRepository.existsByTelefone(telefone)) {
            throw new IllegalArgumentException("Telefone já está em uso.");
        }
        if (arenaRepository.existsByCpfProprietario(cpfProprietario)) {
            throw new IllegalArgumentException("CPF do proprietário já está em uso.");
        }
        if (cnpj != null && arenaRepository.existsByCnpj(cnpj)) {
            throw new IllegalArgumentException("CNPJ já está em uso.");
        }
    }

    private void encodePassword(Arena arena) {
        String encodedPassword = passwordEncoder.encode(arena.getSenha());
        arena.setSenha(encodedPassword);
    }
}
