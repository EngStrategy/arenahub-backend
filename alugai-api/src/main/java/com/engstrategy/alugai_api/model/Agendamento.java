package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.PeriodoAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_agendamento")
    private LocalDate dataAgendamento;

    private LocalTime inicio;

    private LocalTime fim;

    private boolean isFixo;

    private boolean isPrivado;

    @Enumerated(EnumType.STRING)
    private PeriodoAgendamento periodoAgendamentoFixo;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    @Column(name = "jogadores_necessarios")
    private Integer numeroJogadoresNecessarios;

    @Enumerated(EnumType.STRING)
    private TipoEsporte esporte;

    @ManyToOne
    @JoinColumn(name = "quadra_id", nullable = false)
    private Quadra quadra;

    @ManyToOne
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitacaoEntrada> solicitacoes = new ArrayList<>();

}