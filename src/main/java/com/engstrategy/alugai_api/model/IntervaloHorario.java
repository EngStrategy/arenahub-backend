package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.StatusIntervalo;
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
public class IntervaloHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inicio")
    private LocalTime inicio;

    @Column(name = "fim")
    private LocalTime fim;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusIntervalo status;

    @ManyToOne
    @JoinColumn(name = "horario_funcionamento_id", nullable = false)
    private HorarioFuncionamento horarioFuncionamento;

    @OneToMany(mappedBy = "intervaloHorario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlotHorario> slotsHorario = new ArrayList<>();
}
