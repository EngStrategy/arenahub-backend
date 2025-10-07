package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.StatusDisponibilidade;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
public class SlotHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "horario_inicio", nullable = false)
    private LocalTime horarioInicio;

    @Column(name = "horario_fim", nullable = false)
    private LocalTime horarioFim;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_disponibilidade", nullable = false)
    private StatusDisponibilidade statusDisponibilidade;

    @ManyToOne
    @JoinColumn(name = "intervalo_horario_id", nullable = false)
    private IntervaloHorario intervaloHorario;

    @Builder.Default
    @ManyToMany(mappedBy = "slotsHorario")
    private List<Agendamento> agendamentos = new ArrayList<>();
}
