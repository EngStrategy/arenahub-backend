package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable=false)
    private String nome;

    @Column(nullable=true, unique = true)
    private String email;

    @Column(nullable=false, unique = true)
    private String telefone;

    @Column(nullable = true)
    private String senha;

    @Column(name = "url_foto")
    private String urlFoto;

    @CreationTimestamp
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = false;

    @Column(name = "cpf_cnpj", nullable = true, unique = true)
    private String cpfCnpj;
}
