package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.StatusAssinatura;
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
}
