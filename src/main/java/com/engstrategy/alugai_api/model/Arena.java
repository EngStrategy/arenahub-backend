package com.engstrategy.alugai_api.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Arena extends Usuario {

    @Column(unique=true, nullable=false)
    private String cpfProprietario;

    @Column(unique = true)
    private String cnpj;

    @Embedded
    private Endereco endereco;

    private String descricao;

    @Builder.Default
    @OneToMany(mappedBy = "arena", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Quadra> quadras = new HashSet<>();
}
