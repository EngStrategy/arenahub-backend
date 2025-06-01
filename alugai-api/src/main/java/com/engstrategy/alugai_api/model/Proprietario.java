package com.engstrategy.alugai_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Proprietario extends Usuario {

    @Column(unique=true, nullable=false)
    private String cpf;

}
