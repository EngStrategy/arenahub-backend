package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.atleta.AtletaUpdateDTO;
import com.engstrategy.alugai_api.exceptions.UniqueConstraintViolationException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import com.engstrategy.alugai_api.service.AtletaService;
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
    public Atleta criarAtleta(Atleta atleta) {
        validarDadosUnicos(atleta.getEmail(), atleta.getTelefone());

        encodePassword(atleta);
        return atletaRepository.save(atleta);
    }

    @Override
    public Atleta buscarPorId(Long id) {
        return atletaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Atleta não encontrado com ID: " + id));
    }

    @Override
    public Page<Atleta> listarTodos(Pageable pageable) {
        return atletaRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Atleta atualizar(Long id, AtletaUpdateDTO atletaUpdateDTO) {
        Atleta savedAtleta = atletaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Atleta não encontrado com ID: " + id));

        // Verifica se o telefone mudou e se é único
        if (atletaUpdateDTO.getTelefone() != null && !atletaUpdateDTO.getTelefone().equals(savedAtleta.getTelefone())) {
            if (atletaRepository.existsByTelefone(atletaUpdateDTO.getTelefone())) {
                throw new UniqueConstraintViolationException("Telefone já está em uso.");
            }
            savedAtleta.setTelefone(atletaUpdateDTO.getTelefone());
        }

        // Atualiza os campos que não são nulos
        if (atletaUpdateDTO.getNome() != null) {
            savedAtleta.setNome(atletaUpdateDTO.getNome());
        }
        if (atletaUpdateDTO.getUrlFoto() != null) {
            savedAtleta.setUrlFoto(atletaUpdateDTO.getUrlFoto());
        }

        return atletaRepository.save(savedAtleta);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Atleta não encontrado com ID: " + id));
        atletaRepository.delete(atleta);
    }

    private void validarDadosUnicos(String email, String telefone) {
        if (atletaRepository.existsByEmail(email)) {
            throw new UniqueConstraintViolationException("Email já está em uso.");
        }
        if (atletaRepository.existsByTelefone(telefone)) {
            throw new UniqueConstraintViolationException("Telefone já está em uso.");
        }
    }

    private void encodePassword(Atleta atleta) {
        String encodedPassword = passwordEncoder.encode(atleta.getSenha());
        atleta.setSenha(encodedPassword);
    }
}