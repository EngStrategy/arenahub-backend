package com.engstrategy.alugai_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

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

    @ManyToOne
    @JoinColumn(name = "horario_funcionamento_id", nullable = false)
    private HorarioFuncionamento horarioFuncionamento;
}
