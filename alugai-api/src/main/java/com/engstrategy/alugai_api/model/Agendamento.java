package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.PeriodoAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_agendamento")
    private LocalDate dataAgendamento;

    private boolean isFixo;

    private boolean isPublico;

    @Enumerated(EnumType.STRING)
    private PeriodoAgendamento periodoAgendamentoFixo;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    @Column(name = "vagas_disponiveis") // Renomeado de jogadoresNecessarios -> vagasDisponiveis para clareza
    private Integer vagasDisponiveis;

    @Enumerated(EnumType.STRING)
    private TipoEsporte esporte;

    @ManyToOne
    @JoinColumn(name = "quadra_id", nullable = false)
    private Quadra quadra;

    @ManyToOne
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;

    @ManyToOne
    @JoinColumn(name = "agendamento_fixo_id")
    private AgendamentoFixo agendamentoFixo;

    @ManyToMany
    @JoinTable(
            name = "agendamento_slot_horario",
            joinColumns = @JoinColumn(name = "agendamento_id"),
            inverseJoinColumns = @JoinColumn(name = "slot_horario_id")
    )
    private List<SlotHorario> slotsHorario = new ArrayList<>();

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitacaoEntrada> solicitacoes = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "agendamento_participantes",
            joinColumns = @JoinColumn(name = "agendamento_id"),
            inverseJoinColumns = @JoinColumn(name = "atleta_id")
    )
    private Set<Atleta> participantes = new HashSet<>();

    // Métodos utilitários
    public LocalTime getHorarioInicio() {
        return slotsHorario.stream()
                .map(SlotHorario::getHorarioInicio)
                .min(LocalTime::compareTo)
                .orElse(null);
    }

    public LocalTime getHorarioFim() {
        return slotsHorario.stream()
                .map(SlotHorario::getHorarioFim)
                .max(LocalTime::compareTo)
                .orElse(null);
    }

    public BigDecimal getValorTotal() {
        return slotsHorario.stream()
                .map(SlotHorario::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}