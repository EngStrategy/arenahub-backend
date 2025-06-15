package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.CodigoVerificacao;
import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import com.engstrategy.alugai_api.repository.CodigoVerificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CodigoVerificacaoService {
    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
    private final AtletaRepository atletaRepository;
    private final ArenaRepository arenaRepository;
    private final UserService userService;

    public Optional<CodigoVerificacao> getCode(String code, String email) {
        return codigoVerificacaoRepository.findByCodeAndEmail(code, email);
    }

    @Transactional
    public void confirmCode(CodigoVerificacao codigo) {
        codigo.setConfirmedAt(LocalDateTime.now());
        Usuario usuario = userService.findUserByEmail(codigo.getEmail());

        if(usuario == null) {
            throw new UserNotFoundException("Usuário não encontrado");
        }

        usuario.setAtivo(true);

        if(usuario instanceof Atleta) {
            atletaRepository.save((Atleta) usuario);
        }

        if(usuario instanceof Arena) {
            arenaRepository.save((Arena) usuario);
        }

        codigoVerificacaoRepository.save(codigo);
    }
}
