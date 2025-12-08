package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.PeriodoAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamentoFixo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoFixo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    private PeriodoAgendamento periodo;

    @Enumerated(EnumType.STRING)
    private StatusAgendamentoFixo status;

    @ManyToOne
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;

    @Column(name = "nome_aula")
    private String nomeAula;

    @Column(name = "limite_atletas")
    private Integer limiteAtletas;

    @Column(name = "valor_base_mensal")
    private BigDecimal valorBaseMensal;

    @Column(name = "valor_plano_trimestral")
    private BigDecimal valorPlanoTrimestral;

    @Column(name = "valor_plano_semestral")
    private BigDecimal valorPlanoSemestral;

    @Column(name = "is_elegivel_wellhub")
    private boolean isElegivelWellhub;

    @Column(name = "dias_e_slots_json")
    private String diasESlotsJson;

    @Builder.Default
    @OneToMany(mappedBy = "agendamentoFixo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agendamento> agendamentos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusAgendamentoFixo.ATIVO;
        }
    }
}

