package com.engstrategy.arenahub_api.service.impl;

import com.engstrategy.arenahub_api.exceptions.InvalidPasswordException;
import com.engstrategy.arenahub_api.exceptions.UserNotFoundException;
import com.engstrategy.arenahub_api.jwt.CustomUserDetails;
import com.engstrategy.arenahub_api.model.Arena;
import com.engstrategy.arenahub_api.model.Atleta;
import com.engstrategy.arenahub_api.model.Endereco;
import com.engstrategy.arenahub_api.model.Usuario;
import com.engstrategy.arenahub_api.model.enums.Role;
import com.engstrategy.arenahub_api.repository.ArenaRepository;
import com.engstrategy.arenahub_api.repository.AtletaRepository;
import com.engstrategy.arenahub_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ArenaRepository arenaRepository;
    private final AtletaRepository atletaRepository;
    private final PasswordEncoder passwordEncoder;

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

    @Override
    @Transactional
    public void deleteAccount(String password, Usuario userParam) {
        // Recarregar o usuário para garantir que está no Persistence Context atual
        Usuario currentUsuario = findUserById(userParam.getId(), userParam.getRole());
        if (currentUsuario == null) {
            throw new UserNotFoundException("Usuário não encontrado.");
        }

        if (!passwordEncoder.matches(password, currentUsuario.getSenha())) {
            throw new InvalidPasswordException("A senha fornecida está incorreta.");
        }

        String anonId = currentUsuario.getId().toString();
        
        // Anonimização básica para qualquer Usuario
        currentUsuario.setNome("Conta deletada");
        currentUsuario.setEmail("deleted_" + anonId + "@arenahub.com.br");
        currentUsuario.setTelefone("deleted_" + anonId);
        currentUsuario.setSenha(null);
        currentUsuario.setUrlFoto(null);
        currentUsuario.setCpfCnpj("deleted_" + anonId);
        currentUsuario.setAtivo(false);

        if (currentUsuario instanceof Arena arena) {
            // Anonimização específica para Arena
            arena.setCpfProprietario("deleted_" + anonId);
            arena.setCnpj("deleted_" + anonId);
            arena.setChavePix(null);
            
            Endereco enderecoAnonimo = Endereco.builder()
                    .cep("00000000")
                    .estado("EX")
                    .cidade("EXCLUÍDO")
                    .bairro("EXCLUÍDO")
                    .rua("EXCLUÍDO")
                    .numero("0")
                    .latitude(0.0)
                    .longitude(0.0)
                    .build();
            arena.setEndereco(enderecoAnonimo);
            arenaRepository.save(arena);
        } else if (currentUsuario instanceof Atleta atleta) {
            // Anonimizar dados de pagamento em agendamentos do atleta
            if (atleta.getAgendamentos() != null) {
                atleta.getAgendamentos().forEach(agendamento -> {
                    agendamento.setNomePagadorPix("Conta deletada");
                    agendamento.setTelefonePagadorPix("deleted_" + anonId);
                });
            }
            atletaRepository.save(atleta);
        }
    }
}
