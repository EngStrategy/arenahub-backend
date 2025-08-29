package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.model.enums.Role;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserService extends UserDetailsService {

    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);
    Usuario findUserByEmail(String email);
    Usuario findUserById(UUID id, Role role);
}