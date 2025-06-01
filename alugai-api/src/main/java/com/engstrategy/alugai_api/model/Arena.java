package com.engstrategy.alugai_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Arena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cnpj;

    @Embedded
    private Endereco endereco;

    private String telefone;
    private String descricao;
    private String horarioFuncionamento;
    private String regrasGerais;

    @ManyToOne
    @JoinColumn(name = "proprietario_id", nullable = false)
    private Proprietario proprietario;

    @OneToMany(mappedBy = "arena", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quadra> quadras = new ArrayList<>();
}
