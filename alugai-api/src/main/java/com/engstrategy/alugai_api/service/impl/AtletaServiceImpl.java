package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.atleta.AtletaCreateDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaResponseDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaUpdateDTO;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import com.engstrategy.alugai_api.service.AtletaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtletaServiceImpl implements AtletaService {

    private final AtletaRepository atletaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AtletaServiceImpl(AtletaRepository atletaRepository, PasswordEncoder passwordEncoder) {
        this.atletaRepository = atletaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AtletaResponseDTO criarAtleta(AtletaCreateDTO atletaCreateDTO) {
        validarDadosUnicos(atletaCreateDTO.getEmail(), atletaCreateDTO.getTelefone());

        Atleta atleta = new Atleta();
        atleta.setNome(atletaCreateDTO.getNome());
        atleta.setEmail(atletaCreateDTO.getEmail());
        atleta.setTelefone(atletaCreateDTO.getTelefone());
        atleta.setSenha(passwordEncoder.encode(atletaCreateDTO.getSenha()));
        atleta.setUrlFoto(atletaCreateDTO.getUrlFoto());

        Atleta atletaSalvo = atletaRepository.save(atleta);
        return converterParaResponseDTO(atletaSalvo);
    }

    @Override
    public AtletaResponseDTO buscarPorId(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado com ID: " + id));
        return converterParaResponseDTO(atleta);
    }

    @Override
    public Page<AtletaResponseDTO> listarTodos(Pageable pageable) {
        return atletaRepository.findAll(pageable)
                .map(this::converterParaResponseDTO);
    }

    @Override
    @Transactional
    public AtletaResponseDTO atualizar(Long id, AtletaUpdateDTO atletaUpdateDTO) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado com ID: " + id));

        // Verifica se o telefone mudou e se é único
        if (atletaUpdateDTO.getTelefone() != null && !atletaUpdateDTO.getTelefone().equals(atleta.getTelefone())) {
            if (atletaRepository.existsByTelefone(atletaUpdateDTO.getTelefone())) {
                throw new IllegalArgumentException("Telefone já está em uso.");
            }
            atleta.setTelefone(atletaUpdateDTO.getTelefone());
        }

        // Atualiza os campos que não são nulos
        if (atletaUpdateDTO.getNome() != null) {
            atleta.setNome(atletaUpdateDTO.getNome());
        }
        if (atletaUpdateDTO.getUrlFoto() != null) {
            atleta.setUrlFoto(atletaUpdateDTO.getUrlFoto());
        }

        Atleta atletaAtualizado = atletaRepository.save(atleta);
        return converterParaResponseDTO(atletaAtualizado);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado com ID: " + id));
        atletaRepository.delete(atleta);
    }

    private void validarDadosUnicos(String email, String telefone) {
        if (atletaRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já está em uso.");
        }
        if (atletaRepository.existsByTelefone(telefone)) {
            throw new IllegalArgumentException("Telefone já está em uso.");
        }
    }

    private AtletaResponseDTO converterParaResponseDTO(Atleta atleta) {
        AtletaResponseDTO responseDTO = new AtletaResponseDTO();
        responseDTO.setId(atleta.getId());
        responseDTO.setNome(atleta.getNome());
        responseDTO.setEmail(atleta.getEmail());
        responseDTO.setTelefone(atleta.getTelefone());
        responseDTO.setUrlFoto(atleta.getUrlFoto());
        responseDTO.setDataCriacao(atleta.getDataCriacao());
        return responseDTO;
    }
}