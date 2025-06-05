package com.engstrategy.alugai_api.model;

import jakarta.persistence.*;
import lombok.*;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String nome;

    @Column(nullable=false, unique = true)
    private String email;

    @Column(nullable=false, unique = true)
    private String telefone;

    @Column(nullable = false)
    private String senha;

    @Column(name = "url_foto")
    private String urlFoto;

}
