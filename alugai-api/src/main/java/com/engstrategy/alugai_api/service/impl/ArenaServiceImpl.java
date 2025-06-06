package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.arena.ArenaCreateDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaResponseDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaUpdateDTO;
import com.engstrategy.alugai_api.dto.arena.EnderecoDTO;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Endereco;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.specs.ArenaSpecs;
import com.engstrategy.alugai_api.service.ArenaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArenaServiceImpl implements ArenaService {

    private final ArenaRepository arenaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ArenaServiceImpl(ArenaRepository arenaRepository, PasswordEncoder passwordEncoder) {
        this.arenaRepository = arenaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ArenaResponseDTO criarArena(ArenaCreateDTO arenaCreateDTO) {
        validarDadosUnicos(arenaCreateDTO.getEmail(), arenaCreateDTO.getTelefone(),
                arenaCreateDTO.getCpfProprietario(), arenaCreateDTO.getCnpj());

        Arena arena = new Arena();
        arena.setNome(arenaCreateDTO.getNome());
        arena.setEmail(arenaCreateDTO.getEmail());
        arena.setTelefone(arenaCreateDTO.getTelefone());
        arena.setSenha(passwordEncoder.encode(arenaCreateDTO.getSenha()));
        arena.setCpfProprietario(arenaCreateDTO.getCpfProprietario());
        arena.setCnpj(arenaCreateDTO.getCnpj());
        arena.setDescricao(arenaCreateDTO.getDescricao());
        arena.setUrlFoto(arenaCreateDTO.getUrlFoto());
        arena.setEndereco(converterParaEndereco(arenaCreateDTO.getEndereco()));

        Arena arenaSalva = arenaRepository.save(arena);
        return converterParaResponseDTO(arenaSalva);
    }

    @Override
    public ArenaResponseDTO buscarPorId(Long id) {
        Arena arena = arenaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada com ID: " + id));
        return converterParaResponseDTO(arena);
    }

    @Override
    public Page<ArenaResponseDTO> listarTodos(Pageable pageable, String cidade, String esporte) {
        // Criar Specification base
        Specification<Arena> spec = Specification.where(null);

        // Adicionar filtro de cidade se fornecido
        if (cidade != null && !cidade.trim().isEmpty()) {
            spec = spec.and(ArenaSpecs.hasCidade(cidade));
        }

        // Adicionar filtro de esporte se fornecido
        if (esporte != null && !esporte.trim().isEmpty()) {
            spec = spec.and(ArenaSpecs.hasEsporte(esporte));
        }

        // Executar consulta com specifications
        return arenaRepository.findAll(spec, pageable)
                .map(this::converterParaResponseDTO);
    }

    @Override
    @Transactional
    public ArenaResponseDTO atualizar(Long id, ArenaUpdateDTO arenaUpdateDTO) {
        Arena arena = arenaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Arena não encontrada com ID: " + id));

        // Verifica se o telefone mudou e se é único
        if (arenaUpdateDTO.getTelefone() != null && !arenaUpdateDTO.getTelefone().equals(arena.getTelefone())) {
            if (arenaRepository.existsByTelefone(arenaUpdateDTO.getTelefone())) {
                throw new IllegalArgumentException("Telefone já está em uso.");
            }
            arena.setTelefone(arenaUpdateDTO.getTelefone());
        }

        // Atualiza os campos que não são nulos
        if (arenaUpdateDTO.getNome() != null) {
            arena.setNome(arenaUpdateDTO.getNome());
        }
        if (arenaUpdateDTO.getDescricao() != null) {
            arena.setDescricao(arenaUpdateDTO.getDescricao());
        }
        if (arenaUpdateDTO.getUrlFoto() != null) {
            arena.setUrlFoto(arenaUpdateDTO.getUrlFoto());
        }
        if (arenaUpdateDTO.getEndereco() != null) {
            arena.setEndereco(converterParaEndereco(arenaUpdateDTO.getEndereco()));
        }

        Arena arenaAtualizada = arenaRepository.save(arena);
        return converterParaResponseDTO(arenaAtualizada);
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

    private Endereco converterParaEndereco(EnderecoDTO enderecoDTO) {
        Endereco endereco = new Endereco();
        endereco.setCep(enderecoDTO.getCep());
        endereco.setEstado(enderecoDTO.getEstado());
        endereco.setCidade(enderecoDTO.getCidade());
        endereco.setBairro(enderecoDTO.getBairro());
        endereco.setRua(enderecoDTO.getRua());
        endereco.setNumero(enderecoDTO.getNumero());
        endereco.setComplemento(enderecoDTO.getComplemento());
        return endereco;
    }

    private EnderecoDTO converterParaEnderecoDTO(Endereco endereco) {
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setCep(endereco.getCep());
        enderecoDTO.setEstado(endereco.getEstado());
        enderecoDTO.setCidade(endereco.getCidade());
        enderecoDTO.setBairro(endereco.getBairro());
        enderecoDTO.setRua(endereco.getRua());
        enderecoDTO.setNumero(endereco.getNumero());
        enderecoDTO.setComplemento(endereco.getComplemento());
        return enderecoDTO;
    }

    private ArenaResponseDTO converterParaResponseDTO(Arena arena) {
        ArenaResponseDTO responseDTO = new ArenaResponseDTO();
        responseDTO.setId(arena.getId());
        responseDTO.setNome(arena.getNome());
        responseDTO.setEmail(arena.getEmail());
        responseDTO.setTelefone(arena.getTelefone());
        responseDTO.setCpfProprietario(arena.getCpfProprietario());
        responseDTO.setCnpj(arena.getCnpj());
        responseDTO.setDescricao(arena.getDescricao());
        responseDTO.setUrlFoto(arena.getUrlFoto());
        responseDTO.setEndereco(converterParaEnderecoDTO(arena.getEndereco()));
        responseDTO.setDataCriacao(arena.getDataCriacao());
        return responseDTO;
    }
}
