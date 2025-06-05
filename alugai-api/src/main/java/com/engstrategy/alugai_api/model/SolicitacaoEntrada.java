package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.StatusSolicitacao;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @ManyToOne
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta solicitante;

    @Enumerated(EnumType.STRING)
    private StatusSolicitacao status;
}
