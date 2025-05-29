package com.engstrategy.alugai_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
@Getter
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable=false)
    private String nome;

    @Column(nullable=false, unique = true)
    private String email;

    @Column(nullable=false, unique = true)
    private String telefone;

    @Column(nullable = false)
    private String senha;



}
