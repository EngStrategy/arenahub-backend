package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.agendamento.arena.CidadeDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaUpdateDTO;
import com.engstrategy.alugai_api.dto.arena.CidadeResponseDTO;
import com.engstrategy.alugai_api.exceptions.UniqueConstraintViolationException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.mapper.EnderecoMapper;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.CodigoVerificacao;
import com.engstrategy.alugai_api.model.Usuario;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArenaServiceImpl implements ArenaService {

    private final ArenaRepository arenaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnderecoMapper enderecoMapper;
    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
    private final EmailService emailService;
    private final UserService userService;

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
        Specification<Arena> spec = ArenaSpecs.isAtivo();

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
        if(arenaUpdateDTO.getUrlFoto() == null) {
            savedArena.setUrlFoto(null);
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
        if (userService.existsByEmail(email)) {
            throw new UniqueConstraintViolationException("Email já está em uso.");
        }
        if (userService.existsByTelefone(telefone)) {
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

    @Override
    @Transactional
    public void redefinirSenha(Usuario usuario, String novaSenha) {
        if (!(usuario instanceof Arena)) {
            throw new IllegalArgumentException("Usuário não é uma Arena");
        }
        Arena arena = (Arena) usuario;
        arena.setSenha(passwordEncoder.encode(novaSenha));
        arenaRepository.save(arena);
    }

    @Override
    @Transactional
    public void alterarSenha(Long arenaId, String senhaAtual, String novaSenha) {
        Arena arena = arenaRepository.findById(arenaId)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada"));

        if (!passwordEncoder.matches(senhaAtual, arena.getSenha())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }

        arena.setSenha(passwordEncoder.encode(novaSenha));
        arenaRepository.save(arena);
    }

    @Override
    public List<CidadeDTO> getCidades() {
        return arenaRepository.findDistinctCidadeAndEstado().stream()
                .map(result -> new CidadeDTO((String) result[0], (String) result[1]))
                .collect(Collectors.toList());
    }

}
