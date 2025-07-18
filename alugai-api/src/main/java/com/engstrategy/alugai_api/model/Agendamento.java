package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.PeriodoAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusSolicitacao;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // Campos desnormalizados para preservar informações históricas
    @Column(name = "horario_inicio_snapshot")
    private LocalTime horarioInicioSnapshot;

    @Column(name = "horario_fim_snapshot")
    private LocalTime horarioFimSnapshot;

    @Column(name = "valor_total_snapshot")
    private BigDecimal valorTotalSnapshot;

    @Column(name = "data_snapshot")
    private LocalDateTime dataSnapshot;

    private boolean isFixo;
    private boolean isPublico;

    @Enumerated(EnumType.STRING)
    private PeriodoAgendamento periodoAgendamentoFixo;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    @Column(name = "vagas_disponiveis")
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
        // Prioriza snapshot se disponível, senão calcula dos slots
        if (horarioInicioSnapshot != null) {
            return horarioInicioSnapshot;
        }
        return slotsHorario.stream()
                .map(SlotHorario::getHorarioInicio)
                .min(LocalTime::compareTo)
                .orElse(null);
    }

    public LocalTime getHorarioFim() {
        if (horarioFimSnapshot != null) {
            return horarioFimSnapshot;
        }
        return slotsHorario.stream()
                .map(SlotHorario::getHorarioFim)
                .max(LocalTime::compareTo)
                .orElse(null);
    }

    public BigDecimal getValorTotal() {
        if (valorTotalSnapshot != null) {
            return valorTotalSnapshot;
        }
        return slotsHorario.stream()
                .map(SlotHorario::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Método para criar snapshot dos dados
    public void criarSnapshot() {
        this.horarioInicioSnapshot = getHorarioInicio();
        this.horarioFimSnapshot = getHorarioFim();
        this.valorTotalSnapshot = getValorTotal();
        this.dataSnapshot = LocalDateTime.now();
    }

    public boolean possuiSolicitacoes() {
        return solicitacoes != null && solicitacoes.stream()
                .anyMatch(solicitacao -> solicitacao.getStatus() == StatusSolicitacao.PENDENTE);
    }
}