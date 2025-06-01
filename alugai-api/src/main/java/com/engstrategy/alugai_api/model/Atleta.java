package com.engstrategy.alugai_api.model;

import jakarta.persistence.Entity;

import java.util.UUID;

@Entity
public class Atleta extends Usuario {

    // Construtor vazio (necessário para o JPA)
    public Atleta() {
    }

    // Construtor com os campos herdados de Usuario, se necessário
    public Atleta(UUID id, String nome, String email, String telefone, String senha) {
        super(id, nome, email, telefone, senha);
    }
}
