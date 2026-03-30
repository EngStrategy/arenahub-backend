package com.engstrategy.arenahub_api.model;

import com.engstrategy.arenahub_api.model.enums.DiaDaSemana;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioFuncionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DiaDaSemana diaDaSemana;

    @Builder.Default
    @OneToMany(mappedBy = "horarioFuncionamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IntervaloHorario> intervalosDeHorario = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "quadra_id", nullable = false)
    private Quadra quadra;
}