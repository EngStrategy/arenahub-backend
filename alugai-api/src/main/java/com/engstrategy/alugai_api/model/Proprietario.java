package com.engstrategy.alugai_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Proprietario extends Usuario {

    @Column(unique=true, nullable=false)
    private String cpf;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

}
