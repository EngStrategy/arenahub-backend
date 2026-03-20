package com.engstrategy.arenahub_api.model;

import com.engstrategy.arenahub_api.model.enums.FormaPagamento;
import com.engstrategy.arenahub_api.model.enums.StatusAssinatura;
import com.engstrategy.arenahub_api.model.enums.TipoChavePix;
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

    @Column(name = "horas_cancelar_agendamento")
    private Integer horasCancelarAgendamento;

    private String descricao;

    @Builder.Default
    @OneToMany(mappedBy = "arena", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Quadra> quadras = new HashSet<>();

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_assinatura")
    private StatusAssinatura statusAssinatura;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    @Builder.Default
    private FormaPagamento formaPagamento = FormaPagamento.LOCAL;

    @Column(name = "chave_pix")
    private String chavePix;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chave_pix")
    private TipoChavePix tipoChavePix;
}
