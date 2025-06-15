package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.arena.ArenaUpdateDTO;
import com.engstrategy.alugai_api.exceptions.UniqueConstraintViolationException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.mapper.EnderecoMapper;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.CodigoVerificacao;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.CodigoVerificacaoRepository;
import com.engstrategy.alugai_api.repository.specs.ArenaSpecs;
import com.engstrategy.alugai_api.service.ArenaService;
import com.engstrategy.alugai_api.util.GeradorCodigoVerificacao;
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
    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public Arena criarArena(Arena arena) {
        validarDadosUnicos(arena.getEmail(), arena.getTelefone(),
                arena.getCpfProprietario(), arena.getCnpj());

        encodePassword(arena);
        Arena savedArena = arenaRepository.save(arena);

        CodigoVerificacao codigoVerificacao = GeradorCodigoVerificacao.gerarCodigoVerificacao(savedArena.getEmail());
        codigoVerificacaoRepository.save(codigoVerificacao);

        emailService.enviarCodigoVerificacao(arena.getEmail(), arena.getNome(), codigoVerificacao.getCode());

        return savedArena;
    }

    @Override
    public Arena buscarPorId(Long id) {
        return arenaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + id));
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
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + id));

        if (arenaUpdateDTO.getTelefone() != null && !arenaUpdateDTO.getTelefone().equals(savedArena.getTelefone())) {
            if (arenaRepository.existsByTelefone(arenaUpdateDTO.getTelefone())) {
                throw new UniqueConstraintViolationException("Telefone já está em uso.");
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
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + id));
        arenaRepository.delete(arena);
    }

    private void validarDadosUnicos(String email, String telefone, String cpfProprietario, String cnpj) {
        if (arenaRepository.existsByEmail(email)) {
            throw new UniqueConstraintViolationException("Email já está em uso.");
        }
        if (arenaRepository.existsByTelefone(telefone)) {
            throw new UniqueConstraintViolationException("Telefone já está em uso.");
        }
        if (arenaRepository.existsByCpfProprietario(cpfProprietario)) {
            throw new UniqueConstraintViolationException("CPF do proprietário já está em uso.");
        }
        if (cnpj != null && arenaRepository.existsByCnpj(cnpj)) {
            throw new UniqueConstraintViolationException("CNPJ já está em uso.");
        }
    }

    private void encodePassword(Arena arena) {
        String encodedPassword = passwordEncoder.encode(arena.getSenha());
        arena.setSenha(encodedPassword);
    }
}
